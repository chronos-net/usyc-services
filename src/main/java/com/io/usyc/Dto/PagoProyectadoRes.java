package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoProyectadoRes(
        String periodo,          // YYYY-MM (ej: 2025-12)
        LocalDate fechaVencimiento,
        String conceptoCodigo,   // MENSUALIDAD / INSCRIPCION
        String descripcion,   // MENSUALIDAD / INSCRIPCION
        BigDecimal monto,
        String estado,            // PAGADO / PENDIENTE
        // Recibo que justifica PAGADO (opcional)
        Long reciboId,
        String folio,
        LocalDate fechaPago,
        LocalDate fechaEmision
) {}
