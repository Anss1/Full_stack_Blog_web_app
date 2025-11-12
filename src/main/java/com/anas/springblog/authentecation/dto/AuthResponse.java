package com.anas.springblog.authentecation.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
){}
