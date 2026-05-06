package com.io.usyc.Dto;

import java.time.LocalDate;

public record AlumnoListItemRes(
        String alumnoId,
        String nombreCompleto,
        String matricula,
        Boolean activo,
        LocalDate fechaIngreso,
        LocalDate fechaTermino,

        Long escolaridadId,
        String escolaridadNombre,

        String carreraId,
        String carreraNombre,

        Integer plantelId,
        String plantelNombre,
        String plantelCode
) {}
