package com.io.usyc.Service;

import com.io.usyc.Dto.AlumnoCreateReq;
import com.io.usyc.Dto.AlumnoRes;
import com.io.usyc.Dto.AlumnoUpdateReq;

public interface AlumnoService {
    AlumnoRes crear(AlumnoCreateReq req);
    AlumnoRes obtener(String alumnoId);
    AlumnoRes actualizar(String alumnoId, AlumnoUpdateReq req);

}