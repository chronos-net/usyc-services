package com.io.usyc.Dto;

public record ConceptoRes(
        Long conceptoId,
        String codigo,
        String nombre,
        String descripcion,
        String tipoMonto,
        Boolean activo
) {}