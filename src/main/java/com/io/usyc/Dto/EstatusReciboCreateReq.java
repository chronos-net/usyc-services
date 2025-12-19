package com.io.usyc.Dto;

public record EstatusReciboCreateReq(
        String codigo,   // EMITIDO, PAGADO, CANCELADO
        String nombre
) {}
