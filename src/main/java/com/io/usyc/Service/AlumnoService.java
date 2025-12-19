package com.io.usyc.Service;

import com.io.usyc.Dto.AlumnoCreateReq;
import com.io.usyc.Dto.AlumnoRes;

public interface AlumnoService {
    AlumnoRes crear(AlumnoCreateReq req);
    AlumnoRes obtener(String alumnoId);
}