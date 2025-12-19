package com.io.usyc.Dto;

public record ConceptoUpdateReq(
        String nombre,
        String descripcion,
        String tipoMonto,
        Boolean activo
) {}
