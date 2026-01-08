package com.io.usyc.Dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserUpdateReq", description = "Request para actualizar datos del usuario (sin contraseña)")
public record UserUpdateReq(
        @Schema(example = "alfred@usyc.com", description = "Email (opcional)")
        String email,

        @Schema(example = "alfred", description = "Username (opcional)")
        String username,

        @Schema(example = "Alfred Pennyworth", description = "Nombre completo (opcional)")
        String fullName,

        @Schema(example = "123456789", description = "AlumnoId (opcional, 9 chars)")
        String alumnoId,

        @Schema(example = "true", description = "Activo (opcional)")
        Boolean active,

        @Schema(example = "3", description = "Plantel ID (opcional, si se manda debe existir)")
        Integer plantelId
) {}

