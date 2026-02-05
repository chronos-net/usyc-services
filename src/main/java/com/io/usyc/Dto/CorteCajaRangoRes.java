package com.io.usyc.Dto;

import java.time.LocalDate;
import java.util.List;

public record CorteCajaRangoRes(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer plantelId,
        ResumenCorteDto resumen,
        List<ResumenPorTipoPagoDto> porTipoPago,
        List<ReciboCorteItemDto> recibos
) {}

