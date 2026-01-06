package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CorteCajaDiarioRes(
        LocalDate fecha,
        Integer plantelId,
        ResumenCorteDto resumen,
        List<ResumenPorTipoPagoDto> porTipoPago,
        List<ReciboCorteItemDto> recibos
) {}