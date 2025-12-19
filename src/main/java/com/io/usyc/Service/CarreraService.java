package com.io.usyc.Service;

import com.io.usyc.Dto.CarreraCreateReq;
import com.io.usyc.Dto.CarreraRes;
import com.io.usyc.Dto.CarreraUpdateReq;

import java.util.List;

public interface CarreraService {
    CarreraRes crear(CarreraCreateReq req);
    CarreraRes actualizar(String carreraId, CarreraUpdateReq req);
    CarreraRes obtener(String carreraId);
    List<CarreraRes> listar(Boolean soloActivos);
    List<CarreraRes> listarPorEscolaridad(Long escolaridadId, Boolean soloActivos);
    void activar(String carreraId);
    void desactivar(String carreraId);
}
