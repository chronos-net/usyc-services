package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReciboRes(
        Long reciboId,
        String folio,
        LocalDate fechaEmision,
        LocalDate fechaPago,
        String alumnoId,
        String alumnoNombre,
        String concepto,
        BigDecimal monto,
        String moneda,
        String estatusCodigo,
        String estatusNombre,
        Integer tipoPagoId,
        String tipoPagoCodigo,
        String tipoPagoNombre,
        Boolean cancelado,
        String qrPayload
) {}
