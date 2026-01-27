package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.util.List;

public record CarreraCreateReq(
        String carreraId,
        Long escolaridadId,
        String nombre,
        Integer duracionAnios,
        Integer duracionMeses,
        Boolean activo,
        List<CarreraConceptoConfigReq> conceptos
) {}