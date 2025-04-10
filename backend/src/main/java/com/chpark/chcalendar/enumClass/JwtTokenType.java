package com.chpark.chcalendar.enumClass;

import lombok.Getter;

@Getter
public enum JwtTokenType {
    ACCESS("jwtToken"),
    REFRESH("jwtRefreshToken");

    private final String value;

    JwtTokenType(String value) {
        this.value = value;
    }
}
