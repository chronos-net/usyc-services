package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReciboCrearReq(
        String alumnoId,
        String concepto,          // INSCRIPCION | MENSUALIDAD | OTRO
        BigDecimal montoManual,   // obligatorio si concepto=OTRO
        LocalDate fechaPago,      // si null = hoy
        String comentario
) {}
