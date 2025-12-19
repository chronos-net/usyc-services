package com.io.usyc.Service;

import com.io.usyc.Dto.EscolaridadCreateReq;
import com.io.usyc.Dto.EscolaridadRes;
import com.io.usyc.Dto.EscolaridadUpdateReq;

import java.util.List;

public interface EscolaridadService {
    EscolaridadRes crear(EscolaridadCreateReq req);
    EscolaridadRes actualizar(Long id, EscolaridadUpdateReq req);
    EscolaridadRes obtener(Long id);
    EscolaridadRes obtenerPorCodigo(String codigo);
    List<EscolaridadRes> listar(Boolean soloActivos);
    void activar(Long id);
    void desactivar(Long id);
}
