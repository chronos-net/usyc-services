package com.io.usyc.Dto;

import java.util.List;

public record AuthRegisterReq(
        String username,
        String email,
        String password,
        String fullName,
        java.util.List<String> roleCodes
) {}

