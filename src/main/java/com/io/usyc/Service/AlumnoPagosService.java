package com.io.usyc.Service;

import com.io.usyc.Dto.AlumnoPagosResumenRes;
import com.io.usyc.Dto.ReciboRes;

import java.util.List;

public interface AlumnoPagosService {
    AlumnoPagosResumenRes obtenerResumen(String alumnoId);

    List<ReciboRes> obtenerPagos(SecurityUserDetails securityUserDetails);
}