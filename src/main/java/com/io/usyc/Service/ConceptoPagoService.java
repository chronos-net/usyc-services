package com.io.usyc.Service;

import com.io.usyc.Dto.*;

import java.util.List;

public interface ConceptoPagoService {
    ConceptoRes crear(ConceptoCreateReq req);
    ConceptoRes actualizar(Long conceptoId, ConceptoUpdateReq req);
    ConceptoRes obtener(Long conceptoId);
    ConceptoRes obtenerPorCodigo(String codigo);
    List<ConceptoRes> listar(Boolean soloActivos);
    void activar(Long conceptoId);
    void desactivar(Long conceptoId);
}
