package com.io.usyc.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TipoPagoUpdateReq(
        @NotBlank @Size(max = 100) String name,
        @NotNull Boolean active
) {}