package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.Alumno;
import com.io.usyc.Domain.CarreraConceptoConfig;
import com.io.usyc.Domain.Recibo;
import com.io.usyc.Dto.AlumnoPagosResumenRes;
import com.io.usyc.Dto.PagoProyectadoRes;
import com.io.usyc.Dto.ReciboRes;
import com.io.usyc.Repository.AlumnoRepository;
import com.io.usyc.Repository.AppUserRepository;
import com.io.usyc.Repository.CarreraConceptoConfigRepository;
import com.io.usyc.Repository.ReciboRepository;
import com.io.usyc.Service.AlumnoPagosService;
import com.io.usyc.Service.SecurityUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class AlumnoPagosServiceImpl implements AlumnoPagosService {

    private final AlumnoRepository alumnoRepo;
    private final ReciboRepository reciboRepo;
    @Autowired private AppUserRepository appUserRepository;
    @Autowired private CarreraConceptoConfigRepository carreraConceptoRepo;

    public AlumnoPagosServiceImpl(AlumnoRepository alumnoRepo, ReciboRepository reciboRepo) {
        this.alumnoRepo = alumnoRepo;
        this.reciboRepo = reciboRepo;
    }

    @Override
    public AlumnoPagosResumenRes obtenerResumen(String alumnoId) {

        if (alumnoId == null || alumnoId.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'alumnoId' es obligatorio.");
        }

        Alumno alumno = alumnoRepo.findById(alumnoId.trim())
                .orElseThrow(() -> new IllegalArgumentException("No existe alumno con id: " + alumnoId));

        var carrera = alumno.getCarrera();
        if (carrera == null) throw new IllegalArgumentException("El alumno no tiene carrera asignada.");

        LocalDate ingreso = alumno.getFechaIngreso() != null ? alumno.getFechaIngreso() : LocalDate.now();

        LocalDate termino = alumno.getFechaTermino();
        if (termino == null) {
            termino = calcularFechaTermino(ingreso, carrera.getDuracionAnios(), carrera.getDuracionMeses());
        }

        // Configs de carrera (conceptos)
        List<CarreraConceptoConfig> configs = carreraConceptoRepo.findByCarrera_Id(carrera.getId())
                .stream()
                .filter(x -> Boolean.TRUE.equals(x.getActivo()))
                .toList();

        if (configs.isEmpty()) {
            throw new IllegalArgumentException("La carrera no tiene conceptos configurados para proyección.");
        }

        // Pagos reales
        List<Recibo> pagos = reciboRepo.findPagosNoCancelados(alumno.getId());

        // Total pagado
        BigDecimal totalPagado = pagos.stream()
                .map(Recibo::getMonto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Agrupar pagos reales por conceptoCodigo (tu Recibo hoy usa texto: r.getConcepto())
        // Si luego migras a catálogo: r.getConceptoPago().getCodigo()
        var pagosPorConcepto = pagos.stream()
                .filter(r -> r.getConcepto() != null && !r.getConcepto().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        r -> r.getConcepto().trim().toUpperCase(),
                        Collectors.toList()
                ));

        // Construir proyección completa desde configs
        List<PagoProyectadoRes> proyeccion = construirProyeccionDesdeConfigs(
                ingreso,
                termino,
                configs,
                pagosPorConcepto
        );

        BigDecimal totalProyectado = proyeccion.stream()
                .map(PagoProyectadoRes::monto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoPendiente = totalProyectado.subtract(totalPagado);
        if (saldoPendiente.compareTo(BigDecimal.ZERO) < 0) saldoPendiente = BigDecimal.ZERO;

        List<ReciboRes> pagosReales = pagos.stream()
                .map(this::toReciboResSimple)
                .toList();

        // Ya no hay montoMensual / montoInscripcion
        return new AlumnoPagosResumenRes(
                alumno.getId(),
                alumno.getNombreCompleto(),
                carrera.getId(),
                carrera.getNombre(),
                ingreso,
                termino,
                totalPagado,
                totalProyectado,
                saldoPendiente,
                pagosReales,
                proyeccion,alumno.getPlantel().getName()
        );
    }
    private boolean esConceptoMensual(String conceptoCodigo) {
        // AJUSTA A TU CATÁLOGO REAL:
        // Puedes decidir que "COLEGIATURA" sea lo mensual en lugar de "MENSUALIDAD".
        return conceptoCodigo.equalsIgnoreCase("COLEGIATURA")
                || conceptoCodigo.equalsIgnoreCase("MENSUALIDAD");
    }

    private boolean existePagoEnPeriodo(List<Recibo> pagosConcepto, YearMonth periodo) {
        return pagosConcepto.stream().anyMatch(r -> {
            var fecha = (r.getFechaPago() != null) ? r.getFechaPago() : r.getFechaEmision();
            if (fecha == null) return false;
            return YearMonth.from(fecha).equals(periodo);
        });
    }

    private List<PagoProyectadoRes> construirProyeccionDesdeConfigs(
            LocalDate ingreso,
            LocalDate termino,
            List<CarreraConceptoConfig> configs,
            Map<String, List<Recibo>> pagosPorConcepto
    ) {

        List<PagoProyectadoRes> out = new ArrayList<>();

        for (CarreraConceptoConfig cfg : configs) {

            String conceptoCodigo = cfg.getConcepto().getCodigo().trim().toUpperCase();
            BigDecimal monto = cfg.getMonto();
            int cantidad = cfg.getCantidad() != null ? cfg.getCantidad() : 0;

            if (cantidad <= 0) continue;

            // Pagos reales de ese concepto
            List<Recibo> pagosConcepto = pagosPorConcepto.getOrDefault(conceptoCodigo, List.of());

            // 1) Recurrentes mensuales (ej: COLEGIATURA)
            if (esConceptoMensual(conceptoCodigo)) {

                // Genera N meses a partir de ingreso (si termino limita, ajusta)
                YearMonth ym = YearMonth.from(ingreso);
                for (int i = 0; i < cantidad; i++) {
                    YearMonth periodo = ym.plusMonths(i);

                    LocalDate fechaVenc = periodo.atDay(Math.min(ingreso.getDayOfMonth(), periodo.lengthOfMonth()));

                    // Si quieres cortar por termino (opcional):
                    if (fechaVenc.isAfter(termino)) break;

                    boolean pagado = existePagoEnPeriodo(pagosConcepto, periodo);

                    out.add(new PagoProyectadoRes(
                            periodo.toString(),
                            fechaVenc,
                            conceptoCodigo,
                            monto,
                            pagado ? "PAGADO" : "PENDIENTE"
                    ));
                }

            } else {
                // 2) Únicos / eventuales: INSCRIPCION, SERV_ESC, SEGURO, PRACTICAS, TITULACION, etc.
                // Regla simple: vencen en ingreso (puedes moverlos luego por lógica)
                for (int i = 0; i < cantidad; i++) {

                    boolean pagado = i < pagosConcepto.size(); // si hay >= cantidad recibos, los marca pagados

                    out.add(new PagoProyectadoRes(
                            YearMonth.from(ingreso).toString(),
                            ingreso,
                            conceptoCodigo,
                            monto,
                            pagado ? "PAGADO" : "PENDIENTE"
                    ));
                }
            }
        }

        // Orden por fecha
        out.sort(Comparator
                .comparing(PagoProyectadoRes::fechaVencimiento)
                .thenComparing(PagoProyectadoRes::conceptoCodigo));

        return out;
    }


    @Override
    @Transactional(readOnly = true)
    public List<ReciboRes> obtenerPagos(SecurityUserDetails principal) {

        Integer plantelId = (principal.getUser().getPlantel() == null)
                ? null
                : principal.getUser().getPlantel().getId();

        return reciboRepo.findAllVisible(plantelId).stream()
                .map(this::toRes)
                .toList();
    }

    private static final String ESTATUS_CANCELADO = "CANCELADO";

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
    private Integer currentUserPlantelIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }

        String username = auth.getName();

        var user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username));

        return user.getPlantel() != null ? user.getPlantel().getId() : null; // ✅ admin -> null
    }




    // -------------------------
    // Helpers
    // -------------------------

    private static LocalDate calcularFechaTermino(LocalDate ingreso, Integer anios, Integer meses) {
        int a = anios != null ? anios : 0;
        int m = meses != null ? meses : 0;
        if (a == 0 && m == 0) {
            throw new IllegalArgumentException("La carrera tiene duración inválida (0 años, 0 meses).");
        }
        // término = ingreso + duración (si quieres “incluyente”, se puede ajustar 1 mes menos)
        return ingreso.plusYears(a).plusMonths(m);
    }

    private static List<PagoProyectadoRes> construirProyeccionMensualidades(
            LocalDate ingreso,
            LocalDate termino,
            BigDecimal montoMensual,
            Set<YearMonth> mensualidadesPagadas
    ) {
        if (montoMensual == null) montoMensual = BigDecimal.ZERO;

        // Generamos meses desde el mes de ingreso hasta el mes del término (sin pasarnos)
        YearMonth start = YearMonth.from(ingreso);
        YearMonth end = YearMonth.from(termino);

        List<PagoProyectadoRes> out = new ArrayList<>();

        YearMonth current = start;
        while (!current.isAfter(end)) {
            // Definimos vencimiento: mismo día del mes que ingreso, ajustado al fin de mes si no existe
            LocalDate venc = vencimientoDeMes(current, ingreso.getDayOfMonth());

            String estado = mensualidadesPagadas.contains(current) ? "PAGADO" : "PENDIENTE";

            out.add(new PagoProyectadoRes(
                    current.toString(), // "2025-12"
                    venc,
                    "MENSUALIDAD",
                    montoMensual,
                    estado
            ));

            current = current.plusMonths(1);
        }

        return out;
    }

    private static LocalDate vencimientoDeMes(YearMonth ym, int diaDeseado) {
        int ultimoDia = ym.lengthOfMonth();
        int dia = Math.min(diaDeseado, ultimoDia);
        return ym.atDay(dia);
    }

    private ReciboRes toReciboResSimple(Recibo r) {
        var a = r.getAlumno();
        var est = r.getEstatus();

        // cancelado real (por marca o por estatus)
        boolean cancelado = r.getCanceladoEn() != null
                || (est != null && "CANCELADO".equalsIgnoreCase(est.getCodigo()));

        // tipo pago (puede ser null en datos viejos si aún no migras)
        var tp = r.getTipoPago();

        Integer tipoPagoId = tp != null ? tp.getId() : null;
        String tipoPagoCodigo = tp != null ? tp.getCode() : null;
        String tipoPagoNombre = tp != null ? tp.getName() : null;

        return new ReciboRes(
                r.getId(),
                r.getFolio(),r.getFolioLegacy(),
                r.getFechaEmision(),
                r.getFechaPago(),
                a != null ? a.getId() : null,
                a != null ? a.getNombreCompleto() : null,
                r.getConcepto(),
                r.getMonto(),
                r.getMoneda(),
                est != null ? est.getCodigo() : null,
                est != null ? est.getNombre() : null,
                tipoPagoId,
                tipoPagoCodigo,
                tipoPagoNombre,
                cancelado,
                r.getQrPayload(),a.getPlantel().getName()
        );
    }



}

