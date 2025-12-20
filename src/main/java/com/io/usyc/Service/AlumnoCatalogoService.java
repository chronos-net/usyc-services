package com.io.usyc.Service;

import com.io.usyc.Dto.AlumnoListItemRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlumnoCatalogoService {
    Page<AlumnoListItemRes> listar(Pageable pageable);
}