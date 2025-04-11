package com.chpark.chcalendar.dto.security;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JwtAuthenticationResponseDto {
    private String accessToken;
    private String refreshToken;
    private final String tokenType = "Bearer";
    private String message;

    public JwtAuthenticationResponseDto(String accessToken, String refreshToken, String message) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.message = message;
    }
}

