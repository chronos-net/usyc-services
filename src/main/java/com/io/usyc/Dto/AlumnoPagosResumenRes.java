package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AlumnoPagosResumenRes(
        String alumnoId,
        String alumnoNombre,
        String carreraId,
        String carreraNombre,
        LocalDate fechaIngreso,
        LocalDate fechaTermino,
        BigDecimal montoMensual,
        BigDecimal montoInscripcion,

        BigDecimal totalPagado,
        BigDecimal totalProyectado,
        BigDecimal saldoPendiente,

        List<ReciboRes> pagosReales,
        List<PagoProyectadoRes> proyeccion
) {}