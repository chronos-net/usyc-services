package com.io.usyc.Dto;
import java.math.BigDecimal;

public record CarreraUpdateReq(
        Long escolaridadId,
        String nombre,
        Integer duracionAnios,
        Integer duracionMeses,
        Boolean activo,
        java.util.List<CarreraConceptoConfigReq> conceptos
) {}