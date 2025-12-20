package com.io.usyc.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlantelCreateReq(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 250) String address,
        @NotNull Boolean active
) {}
