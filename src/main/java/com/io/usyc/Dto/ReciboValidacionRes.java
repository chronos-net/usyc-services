package com.io.usyc.Dto;

public record ReciboValidacionRes(
        String estado,           // VALIDO | CANCELADO | NO_ENCONTRADO | ALTERADO
        String mensaje,
        ReciboRes recibo
) {}

