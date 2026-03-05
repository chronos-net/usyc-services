package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.*;
import com.io.usyc.Dto.ReciboCrearReq;
import com.io.usyc.Dto.ReciboRes;
import com.io.usyc.Dto.ReciboValidacionRes;
import com.io.usyc.Repository.*;
import com.io.usyc.Service.QrCodeService;
import com.io.usyc.Service.ReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // Ideal: moverlo a application.yml y leerlo por @Value o properties
    private final String llaveQrSecreta = "USYC_LOCAL_SECRET_2025";

    private final ReciboRepository reciboRepo;
    private final AlumnoRepository alumnoRepo;
    private final CatEstatusReciboRepository estatusRepo;
    private final CatTipoPagoRepository tipoPagoRepo;
    private final QrCodeService qrCodeService;

    @Autowired private AppUserRepository appUserRepository;
    @Autowired private CatPlantelRepository catPlantelRepository;

    public ReciboServiceImpl(
            ReciboRepository reciboRepo,
            AlumnoRepository alumnoRepo,
            CatEstatusReciboRepository estatusRepo,
            CatTipoPagoRepository tipoPagoRepo,
            QrCodeService qrCodeService
    ) {
        this.reciboRepo = reciboRepo;
        this.alumnoRepo = alumnoRepo;
        this.estatusRepo = estatusRepo;
        this.tipoPagoRepo = tipoPagoRepo;
        this.qrCodeService = qrCodeService;
    }

    @Override
    public ReciboRes registrarPago(ReciboCrearReq req) {
        validarTexto(req.alumnoId(), "alumnoId");
        validarTexto(req.concepto(), "concepto");
        if (req.tipoPagoId() == null) {
            throw new IllegalArgumentException("El campo 'tipoPagoId' es obligatorio.");
        }


        Alumno alumno = alumnoRepo.findById(req.alumnoId().trim())
                .orElseThrow(() -> new IllegalArgumentException("No existe alumno con id: " + req.alumnoId()));
        Integer plantelId = alumno.getPlantel().getId();

        // ✅ Seguridad por sede: el admin solo puede emitir para su plantel
        if (alumno.getPlantel() == null) {
            throw new IllegalArgumentException("El alumno no tiene plantel asignado.");
        }

        String concepto = req.concepto().trim().toUpperCase();
        LocalDate hoy = LocalDate.now();
        LocalDate fechaPago = (req.fechaPago() == null ? hoy : req.fechaPago());

        //BigDecimal monto = resolverMonto(concepto, alumno.getCarrera(), req.montoManual());

        CatEstatusRecibo pagado = estatusRepo.findByCodigo(ESTATUS_PAGADO)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No está configurado el estatus '" + ESTATUS_PAGADO + "' en catálogo."
                ));

        CatTipoPago tipoPago = tipoPagoRepo.findById(req.tipoPagoId())
                .orElseThrow(() -> new IllegalArgumentException("No existe tipo de pago con id: " + req.tipoPagoId()));

        if (tipoPago.getActive() != null && !Boolean.TRUE.equals(tipoPago.getActive())) {
            throw new IllegalArgumentException("El tipo de pago seleccionado está inactivo.");
        }

        String folio = generarFolioSimple(); // luego lo cambiamos por seq por plantel si quieres

        LocalDateTime ts = LocalDateTime.now();
        String qrPayload = construirQrPayload(folio, alumno.getId(), concepto, req.montoManual(), fechaPago, ts);
        String qrHash = extraerFirmaDePayload(qrPayload);

        String qrFileName = qrCodeService.generarYGuardarQr(folio, qrPayload);

        Recibo r = new Recibo();
        r.setFolio(folio);
        r.setFechaEmision(hoy);
        r.setFechaPago(fechaPago);
        r.setAlumno(alumno);
        r.setConcepto(concepto);
        r.setMonto(req.montoManual());
        r.setMoneda(MONEDA_DEFAULT);
        r.setEstatus(pagado);
        r.setTipoPago(tipoPago);

        // ✅ Guardar sede/plantel
        r.setPlantelId(plantelId);

        r.setQrPayload(qrPayload);
        r.setQrHash(qrHash);
        r.setQrFileName(qrFileName);

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
        if (r.getCanceladoEn() != null ||
                (r.getEstatus() != null && ESTATUS_CANCELADO.equalsIgnoreCase(r.getEstatus().getCodigo()))) {
            return new ReciboValidacionRes("CANCELADO", "El recibo fue cancelado.", toRes(r));
        }

        // Validación de autenticidad
        boolean ok = validarFirmaQr(r);
        if (!ok) {
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
                .orElseThrow(() -> new IllegalArgumentException(
                        "No está configurado el estatus '" + ESTATUS_CANCELADO + "' en catálogo."
                ));

        r.setEstatus(cancelado);
        r.setCanceladoEn(LocalDateTime.now());
        r.setMotivoCancelacion(motivo.trim());
        r.setActualizadoEn(LocalDateTime.now());

        return toRes(reciboRepo.save(r));
    }


    // -------------------------
    // Helpers
    // -------------------------


    private String generarFolioSimple() {
        // Demo/local: REC-000001 (no es concurrente)
        long next = reciboRepo.findAll().stream().mapToLong(Recibo::getId).max().orElse(0L) + 1;
        return "REC-" + String.format("%06d", next);
    }

    /**
     * QR payload final:
     * USYC|<FOLIO>|<TIMESTAMP>|<FIRMA>
     *
     * FIRMA = SHA-256( folio|alumnoId|concepto|monto|fechaPago|timestamp|secret )
     */
    private String construirQrPayload(String folio, String alumnoId, String concepto, BigDecimal monto, LocalDate fechaPago, LocalDateTime ts) {
        String base = folio + "|" + alumnoId + "|" + concepto + "|" + monto.toPlainString() + "|" + fechaPago + "|" + ts + "|" + llaveQrSecreta;
        String firma = sha256Hex(base);
        return "USYC|" + folio + "|" + ts + "|" + firma;
    }

    private boolean validarFirmaQr(Recibo r) {
        String payload = r.getQrPayload();
        String[] parts = (payload == null ? new String[0] : payload.split("\\|"));

        if (parts.length != 4) return false;
        if (!"USYC".equals(parts[0])) return false;

        String folio = parts[1];
        String ts = parts[2];
        String firmaEnQr = parts[3];

        String base = folio + "|" +
                r.getAlumno().getId() + "|" +
                r.getConcepto() + "|" +
                r.getMonto().toPlainString() + "|" +
                r.getFechaPago() + "|" +
                ts + "|" +
                llaveQrSecreta;

        String firmaEsperada = sha256Hex(base);

        if (r.getQrHash() != null && !r.getQrHash().equalsIgnoreCase(firmaEnQr)) {
            return false;
        }

        return firmaEsperada.equalsIgnoreCase(firmaEnQr);
    }

    private String extraerFirmaDePayload(String payload) {
        String[] parts = payload.split("\\|");
        if (parts.length != 4) throw new IllegalArgumentException("QR payload inválido.");
        return parts[3];
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

        boolean cancelado = r.getCanceladoEn() != null
                || (est != null && ESTATUS_CANCELADO.equalsIgnoreCase(est.getCodigo()));

        var tp = r.getTipoPago();

        return new ReciboRes(
                r.getId(),
                r.getFolio(),
                r.getFolioLegacy(),
                r.getFechaEmision(),
                r.getFechaPago(),
                alumno != null ? alumno.getId() : null,
                alumno != null ? alumno.getNombreCompleto() : null,
                r.getConcepto(),
                r.getMonto(),
                r.getMoneda(),
                est != null ? est.getCodigo() : null,
                est != null ? est.getNombre() : null,
                tp != null ? tp.getId() : null,
                tp != null ? tp.getCode() : null,
                tp != null ? tp.getName() : null,
                cancelado,
                r.getQrPayload(),alumno.getPlantel().getName()
        );
    }

    private static void validarTexto(String v, String campo) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
    }

    private Integer currentPlantelId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // funciona con UserDetails estándar

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username));

        if (user.getPlantel() == null) throw new IllegalStateException("Usuario sin plantel.");
        return user.getPlantel().getId();
    }


}
