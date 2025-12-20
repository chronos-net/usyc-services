package com.io.usyc.Dto;

public record TipoPagoRes(
        Integer id,
        String code,
        String name,
        Boolean active
) {}