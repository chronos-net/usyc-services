package com.io.usyc.Dto;

import java.util.List;

public record AuthUserRes(
        Long userId,
        String username,
        String fullName,
        boolean active,
        java.util.List<String> roles,
        Integer plantelId,
        String plantelName
) {}