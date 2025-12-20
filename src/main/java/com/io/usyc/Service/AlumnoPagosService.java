package com.io.usyc.Service;

import com.io.usyc.Dto.AlumnoPagosResumenRes;

public interface AlumnoPagosService {
    AlumnoPagosResumenRes obtenerResumen(String alumnoId);
}