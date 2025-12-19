package com.io.usyc.Dto;

import java.math.BigDecimal;

public record CarreraCreateReq(
        String carreraId,         // "01".."99"
        Long escolaridadId,
        String nombre,
        BigDecimal montoMensual,
        BigDecimal montoInscripcion,
        Boolean activo
) {}