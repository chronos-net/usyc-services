package com.io.usyc.Dto;

import java.math.BigDecimal;

public record CarreraRes(
        String carreraId,
        Long escolaridadId,
        String escolaridadNombre,
        String nombre,
        BigDecimal montoMensual,
        BigDecimal montoInscripcion,
        Integer duracionAnios,
        Integer duracionMeses,
        Boolean activo
) {}