package com.io.usyc.Dto;

import java.util.Set;

public record UserCreateRes(
        Long userId,
        String email,
        String username,
        String fullName,
        boolean active,
        String alumnoId,
        Integer plantelId,
        Set<String> roles
) {}
