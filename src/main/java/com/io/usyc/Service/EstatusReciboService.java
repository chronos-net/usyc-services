package com.io.usyc.Service;

import com.io.usyc.Dto.EstatusReciboCreateReq;
import com.io.usyc.Dto.EstatusReciboRes;
import com.io.usyc.Dto.EstatusReciboUpdateReq;

import java.util.List;

public interface EstatusReciboService {

    EstatusReciboRes crear(EstatusReciboCreateReq req);
    EstatusReciboRes actualizar(Long id, EstatusReciboUpdateReq req);
    EstatusReciboRes obtener(Long id);
    EstatusReciboRes obtenerPorCodigo(String codigo);
    List<EstatusReciboRes> listar();
}
