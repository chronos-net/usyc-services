package com.io.usyc.Dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(name = "UserRes", description = "Respuesta de usuario")
public record UserRes(
        Long userId,
        String email,
        String username,
        String fullName,
        boolean active,
        String alumnoId,
        Integer plantelId,
        Set<String> roles,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
