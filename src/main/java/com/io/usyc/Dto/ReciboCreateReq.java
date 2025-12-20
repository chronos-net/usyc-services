package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReciboCreateReq(
        Long alumnoId,
        String concepto,
        BigDecimal monto,
        LocalDate fechaPago,
        Integer tipoPagoId,
        String comentario
) {}
