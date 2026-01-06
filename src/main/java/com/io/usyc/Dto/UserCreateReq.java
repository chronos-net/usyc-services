package com.io.usyc.Dto;
import java.util.Set;

public record UserCreateReq(
        String email,
        String username,
        String password,
        String fullName,
        Integer plantelId,   // obligatorio
        Set<Long> roleIds    // obligatorio (al menos 1)
) {}
