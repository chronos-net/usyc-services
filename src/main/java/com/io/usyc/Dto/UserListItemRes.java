package com.io.usyc.Dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(name = "UserListItemRes", description = "Elemento de listado de usuarios")
public record UserListItemRes(
        @Schema(example = "10") Long userId,
        @Schema(example = "alfred@usyc.com") String email,
        @Schema(example = "alfred") String username,
        @Schema(example = "Alfred Pennyworth") String fullName,
        @Schema(example = "true") boolean active,
        @Schema(example = "123456789") String alumnoId,
        @Schema(example = "3") Integer plantelId,
        @Schema(example = "Campus Centro") String plantelName,
        @Schema(example = "CENTRO") String plantelCode,
        Set<String> roles,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {}
