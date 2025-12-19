package com.io.usyc.Dto;
import java.math.BigDecimal;

public record CarreraUpdateReq(
        Long escolaridadId,
        String nombre,
        BigDecimal montoMensual,
        BigDecimal montoInscripcion,
        Boolean activo
) {}