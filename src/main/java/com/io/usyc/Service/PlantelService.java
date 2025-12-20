package com.io.usyc.Service;

import com.io.usyc.Dto.*;

import java.util.List;

public interface PlantelService {
    List<PlantelRes> listar(boolean soloActivos);
    PlantelRes crear(PlantelCreateReq req);
    PlantelRes actualizar(Integer id, PlantelUpdateReq req);
    void desactivar(Integer id);
}
