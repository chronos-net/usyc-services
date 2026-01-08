package com.io.usyc.Dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(name = "AlumnoUpdateReq", description = "Request para actualizar datos del alumno")
public record AlumnoUpdateReq(
        @Schema(example = "Bruce Wayne", description = "Nombre completo")
        String nombreCompleto,

        @Schema(example = "MAT-2025-001", description = "Matrícula (única)")
        String matricula,

        @Schema(example = "2", description = "ID escolaridad")
        Long escolaridadId,

        @Schema(example = "05", description = "ID carrera")
        String carreraId,

        @Schema(example = "2025-01-15", description = "Fecha de ingreso (ISO)")
        LocalDate fechaIngreso,

        @Schema(example = "2026-07-01", description = "Fecha de término (ISO, opcional)")
        LocalDate fechaTermino,

        @Schema(example = "true", description = "Activo")
        Boolean activo,

        @Schema(example = "3", description = "Plantel ID")
        Integer plantelId
) {}

