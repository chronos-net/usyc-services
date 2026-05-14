package com.io.usyc.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReciboCrearReq(
        String alumnoId,
        String concepto,
        BigDecimal montoManual,
        LocalDate fechaPago,
        Integer tipoPagoId,
        String comentario,
        /** Periodo de proyección liquidado (YYYY-MM). Opcional; null en pagos extra o legado. */
        String periodoAplicado,
        /** Ordinal de fila de proyección (>=1). Opcional; null en legado. */
        Integer lineaAplicada
) {}
