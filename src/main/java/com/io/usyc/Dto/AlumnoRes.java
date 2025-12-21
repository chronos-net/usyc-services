package com.io.usyc.Dto;

import java.time.LocalDate;

public record AlumnoRes(
        String alumnoId,
        String nombreCompleto,
        String matricula,
        Long escolaridadId,
        String escolaridadNombre,
        String carreraId,
        String carreraNombre,
        Integer plantelId,
        String plantelNombre,
        LocalDate fechaIngreso,
        Boolean activo,Integer recibosPreviosMigrados
) {}
