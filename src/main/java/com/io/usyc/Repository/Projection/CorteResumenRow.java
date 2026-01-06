package com.io.usyc.Repository.Projection;

import java.math.BigDecimal;

public interface CorteResumenRow {
    Long getTotalRecibos();
    BigDecimal getTotalMonto();
    Long getTotalCancelados();
    BigDecimal getTotalMontoCancelado();
}
