package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReciboCrearReq(
        String alumnoId,
        String concepto,
        BigDecimal montoManual,
        LocalDate fechaPago,
        Integer tipoPagoId,
        String comentario
) {}
