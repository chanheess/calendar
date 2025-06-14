package com.chpark.chcalendar.enumClass;

import lombok.Getter;

@Getter
public enum OAuthLoginType {
    OAUTH("oauth"),
    LOCAL("local"),
    LINK("link");

    private final String value;

    OAuthLoginType(String value) {
        this.value = value;
    }
}
