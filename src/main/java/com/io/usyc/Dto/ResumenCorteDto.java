package com.io.usyc.Dto;

import java.math.BigDecimal;

public record ResumenCorteDto(
        long totalRecibos,
        BigDecimal totalMonto,
        long totalCancelados,
        BigDecimal totalMontoCancelado
) {}