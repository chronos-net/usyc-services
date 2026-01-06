package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReciboCorteItemDto(
        Long reciboId,
        String folio,
        String folioLegacy,
        LocalDate fechaEmision,
        LocalDate fechaPago,
        String alumnoId,          // ajusta según tu entidad Alumno
        String alumnoNombre,      // ajusta según tu entidad Alumno
        String concepto,
        BigDecimal monto,
        String moneda,
        Long estatusId,
        String estatusDesc,
        Integer tipoPagoId,
        String tipoPagoDesc,
        boolean cancelado,
        Integer plantelId
) {}
