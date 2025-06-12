package com.chpark.chcalendar.enumClass;

import lombok.Getter;

@Getter
public enum JwtTokenType {
    ACCESS("jwt_token"),
    REFRESH("jwt_refresh_token"),
    GOOGLE_ACCESS("google_access_token"),
    GOOGLE_REFRESH("google_refresh_token");

    private final String value;

    JwtTokenType(String value) {
        this.value = value;
    }
}
