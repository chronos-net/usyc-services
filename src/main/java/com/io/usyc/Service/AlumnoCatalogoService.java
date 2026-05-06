package com.io.usyc.Service;

import com.io.usyc.Dto.AlumnoListItemRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AlumnoCatalogoService {
    Page<AlumnoListItemRes> listar(
            Pageable pageable,
            SecurityUserDetails principal,
            String q,
            Integer escolaridadId,
            Integer plantelId,
            LocalDate fechaIngresoDesde,
            LocalDate fechaIngresoHasta
    );
}