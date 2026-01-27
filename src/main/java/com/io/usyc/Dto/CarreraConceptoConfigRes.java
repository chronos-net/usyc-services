package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.util.List;

public record CarreraConceptoConfigRes(
        Long conceptoId,
        String conceptoCodigo,
        String conceptoNombre,
        BigDecimal monto,
        Integer cantidad,
        Boolean activo
) {}
