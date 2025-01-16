package com.chpark.chcalendar.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JwtAuthenticationResponseDto {
    private String accessToken;
    private final String tokenType = "Bearer";
    private String message;

    public JwtAuthenticationResponseDto(String accessToken, String message) {
        this.accessToken = accessToken;
        this.message = message;
    }
}

