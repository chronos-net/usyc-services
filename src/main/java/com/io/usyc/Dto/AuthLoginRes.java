package com.io.usyc.Dto;

public record AuthLoginRes(
        String message,
        AuthUserRes user
) {}