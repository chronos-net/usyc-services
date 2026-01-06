package com.io.usyc.Repository.Projection;

import java.math.BigDecimal;

public interface CorteTipoPagoRow {
    Integer getTipoPagoId();
    String getTipoPagoDesc();
    Long getTotalRecibos();
    BigDecimal getTotalMonto();
}
