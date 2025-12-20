package com.io.usyc.Service;

import com.io.usyc.Dto.TipoPagoCreateReq;
import com.io.usyc.Dto.TipoPagoRes;
import com.io.usyc.Dto.TipoPagoUpdateReq;

import java.util.List;

public interface TipoPagoService {
    List<TipoPagoRes> listar(boolean soloActivos);
    TipoPagoRes crear(TipoPagoCreateReq req);
    TipoPagoRes actualizar(Integer id, TipoPagoUpdateReq req);
    void desactivar(Integer id);
}