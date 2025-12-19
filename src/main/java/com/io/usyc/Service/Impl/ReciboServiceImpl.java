package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.Alumno;
import com.io.usyc.Domain.CatCarrera;
import com.io.usyc.Domain.CatEstatusRecibo;
import com.io.usyc.Domain.Recibo;
import com.io.usyc.Dto.ReciboCrearReq;
import com.io.usyc.Dto.ReciboRes;
import com.io.usyc.Dto.ReciboValidacionRes;
import com.io.usyc.Repository.AlumnoRepository;
import com.io.usyc.Repository.CatEstatusReciboRepository;
import com.io.usyc.Repository.ReciboRepository;
import com.io.usyc.Service.ReciboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@Transactional
public class ReciboServiceImpl implements ReciboService {

    private static final String MONEDA_DEFAULT = "MXN";
    private static final String ESTATUS_PAGADO = "PAGADO";
    private static final String ESTATUS_CANCELADO = "CANCELADO";

    // Llave secreta local (modo simple). Ideal: ponerlo en application.yml
    private final String llaveQrSecreta = "USYC_LOCAL_SECRET_2025";

    private final ReciboRepository reciboRepo;
    private final AlumnoRepository alumnoRepo;
    private final CatEstatusReciboRepository estatusRepo;

    public ReciboServiceImpl(
            ReciboRepository reciboRepo,
            AlumnoRepository alumnoRepo,
            CatEstatusReciboRepository estatusRepo
    ) {
        this.reciboRepo = reciboRepo;
        this.alumnoRepo = alumnoRepo;
        this.estatusRepo = estatusRepo;
    }

    @Override
    public ReciboRes registrarPago(ReciboCrearReq req) {
        validarTexto(req.alumnoId(), "alumnoId");
        validarTexto(req.concepto(), "concepto");

        Alumno alumno = alumnoRepo.findById(req.alumnoId().trim())
                .orElseThrow(() -> new IllegalArgumentException("No existe alumno con id: " + req.alumnoId()));

        String concepto = req.concepto().trim().toUpperCase();
        LocalDate hoy = LocalDate.now();
        LocalDate fechaPago = (req.fechaPago() == null ? hoy : req.fechaPago());

        BigDecimal monto = resolverMonto(concepto, alumno.getCarrera(), req.montoManual());

        CatEstatusRecibo pagado = estatusRepo.findByCodigo(ESTATUS_PAGADO)
                .orElseThrow(() -> new IllegalArgumentException("No está configurado el estatus '" + ESTATUS_PAGADO + "' en catálogo."));

        String folio = generarFolioSimple(); // REC-000001

        // QR: payload y hash
        String token = generarTokenCorto(); // random simple
        String qrPayload = "USYC|" + folio + "|" + token;

        String qrHash = firmarQr(qrPayload, alumno.getId(), concepto, monto);

        Recibo r = new Recibo();
        r.setFolio(folio);
        r.setFechaEmision(hoy);
        r.setFechaPago(fechaPago);
        r.setAlumno(alumno);
        r.setConcepto(concepto);
        r.setMonto(monto);
        r.setMoneda(MONEDA_DEFAULT);
        r.setEstatus(pagado);
        r.setQrPayload(qrPayload);
        r.setQrHash(qrHash);
        r.setComentario(req.comentario());

        r.setCreadoEn(LocalDateTime.now());
        r.setActualizadoEn(LocalDateTime.now());

        return toRes(reciboRepo.save(r));
    }

    @Override
    @Transactional(readOnly = true)
    public ReciboValidacionRes validarQr(String qrPayload) {
        validarTexto(qrPayload, "qrPayload");

        var opt = reciboRepo.findByQrPayload(qrPayload.trim());
        if (opt.isEmpty()) {
            return new ReciboValidacionRes("NO_ENCONTRADO", "No existe un recibo con ese QR.", null);
        }

        Recibo r = opt.get();

        // Si está cancelado
        if (r.getCanceladoEn() != null || (r.getEstatus() != null && ESTATUS_CANCELADO.equalsIgnoreCase(r.getEstatus().getCodigo()))) {
            return new ReciboValidacionRes("CANCELADO", "El recibo fue cancelado.", toRes(r));
        }

        // Recalcular firma para detectar alteración
        String firmaEsperada = firmarQr(r.getQrPayload(), r.getAlumno().getId(), r.getConcepto(), r.getMonto());
        if (!firmaEsperada.equals(r.getQrHash())) {
            return new ReciboValidacionRes("ALTERADO", "El recibo no pasó la validación de autenticidad.", toRes(r));
        }

        return new ReciboValidacionRes("VALIDO", "Recibo válido.", toRes(r));
    }

    @Override
    public ReciboRes cancelar(Long reciboId, String motivo) {
        if (reciboId == null) throw new IllegalArgumentException("El campo 'reciboId' es obligatorio.");
        validarTexto(motivo, "motivo");

        Recibo r = reciboRepo.findById(reciboId)
                .orElseThrow(() -> new IllegalArgumentException("No existe recibo con id: " + reciboId));

        CatEstatusRecibo cancelado = estatusRepo.findByCodigo(ESTATUS_CANCELADO)
                .orElseThrow(() -> new IllegalArgumentException("No está configurado el estatus '" + ESTATUS_CANCELADO + "' en catálogo."));

        r.setEstatus(cancelado);
        r.setCanceladoEn(LocalDateTime.now());
        r.setMotivoCancelacion(motivo.trim());
        r.setActualizadoEn(LocalDateTime.now());

        return toRes(reciboRepo.save(r));
    }

    // -------------------------
    // Helpers
    // -------------------------

    private BigDecimal resolverMonto(String concepto, CatCarrera carrera, BigDecimal montoManual) {
        if (carrera == null) throw new IllegalArgumentException("El alumno no tiene carrera asignada.");

        return switch (concepto) {
            case "INSCRIPCION" -> carrera.getMontoInscripcion();
            case "MENSUALIDAD" -> carrera.getMontoMensual();
            case "OTRO" -> {
                if (montoManual == null) throw new IllegalArgumentException("Para concepto OTRO es obligatorio 'montoManual'.");
                if (montoManual.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("'montoManual' no puede ser negativo.");
                yield montoManual;
            }
            default -> throw new IllegalArgumentException("Concepto inválido. Usa: INSCRIPCION, MENSUALIDAD u OTRO.");
        };
    }

    private String generarFolioSimple() {
        // Folio simple: REC-000001 basado en ID autoincremental.
        // OJO: Esto requiere guardar primero o usar una secuencia. Solución simple:
        // tomamos el último id y sumamos 1 (en local sirve). Si quieres robusto, hacemos tabla secuencia.
        long next = reciboRepo.findAll().stream().mapToLong(Recibo::getId).max().orElse(0L) + 1;
        return "REC-" + String.format("%06d", next);
    }

    private String generarTokenCorto() {
        // Token simple local (puedes cambiar a UUID)
        return Long.toHexString(System.nanoTime()).toUpperCase();
    }

    private String firmarQr(String qrPayload, String alumnoId, String concepto, BigDecimal monto) {
        // Firma: hash(qrPayload|alumnoId|concepto|monto|llaveSecreta)
        String base = qrPayload + "|" + alumnoId + "|" + concepto + "|" + monto.toPlainString() + "|" + llaveQrSecreta;
        return sha256Hex(base);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar hash SHA-256.", e);
        }
    }

    private ReciboRes toRes(Recibo r) {
        var alumno = r.getAlumno();
        var est = r.getEstatus();

        boolean cancelado = r.getCanceladoEn() != null || (est != null && ESTATUS_CANCELADO.equalsIgnoreCase(est.getCodigo()));

        return new ReciboRes(
                r.getId(),
                r.getFolio(),
                r.getFechaEmision(),
                r.getFechaPago(),
                alumno != null ? alumno.getId() : null,
                alumno != null ? alumno.getNombreCompleto() : null,
                r.getConcepto(),
                r.getMonto(),
                r.getMoneda(),
                est != null ? est.getCodigo() : null,
                est != null ? est.getNombre() : null,
                cancelado,
                r.getQrPayload()
        );
    }

    private static void validarTexto(String v, String campo) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
    }
}
