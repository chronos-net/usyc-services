package com.io.usyc.Dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AlumnoCreateReq(
        String nombreCompleto,
        String matricula,
        Long escolaridadId,
        String carreraId,      // "01".."99" (acepta "1" y lo normaliza)
        LocalDate fechaIngreso, // opcional, si viene null = hoy
        @NotNull Integer plantelId,
        Boolean pullPrevReceipts,
        String prevReceiptsNombre
) {}