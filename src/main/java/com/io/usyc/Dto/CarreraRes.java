package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.util.List;


public record CarreraRes(
        String carreraId,
        Long escolaridadId,
        String escolaridadNombre,
        String nombre,
        Integer duracionAnios,
        Integer duracionMeses,
        Boolean activo,
        BigDecimal totalProyectado,
        List<CarreraConceptoConfigRes> conceptos
) {}