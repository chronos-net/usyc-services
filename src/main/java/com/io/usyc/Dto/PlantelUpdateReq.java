package com.io.usyc.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlantelUpdateReq(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 250) String address,
        @NotNull Boolean active
) {}
