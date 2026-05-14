package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReciboRes(
        Long reciboId,
        String folio,
        String folioLegacy,
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
        String tipoPagoCode,
        String tipoPagoName,
        boolean cancelado,
        String qrPayload,
        String plantel,
        String comentario,
        /** YYYY-MM desde recibo; null en legado. */
        String periodoAplicado,
        /** Ordinal de línea de proyección; null en legado. */
        Integer lineaAplicada
) {}
