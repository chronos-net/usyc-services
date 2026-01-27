package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.util.List;

public record CarreraConceptoConfigReq(
        Long conceptoId,
        java.math.BigDecimal monto,
        Integer cantidad,
        Boolean activo
) {}