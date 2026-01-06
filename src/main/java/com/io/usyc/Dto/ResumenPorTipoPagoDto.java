package com.io.usyc.Dto;

import java.math.BigDecimal;

public record ResumenPorTipoPagoDto(
        Integer tipoPagoId,
        String tipoPagoDesc,
        long totalRecibos,
        BigDecimal totalMonto
) {}
