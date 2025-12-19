package com.io.usyc.Dto;

import java.math.BigDecimal;

public record CarreraCreateReq(
        String carreraId,
        Long escolaridadId,
        String nombre,
        BigDecimal montoMensual,
        BigDecimal montoInscripcion,
        Integer duracionAnios,
        Integer duracionMeses,
        Boolean activo
) {}