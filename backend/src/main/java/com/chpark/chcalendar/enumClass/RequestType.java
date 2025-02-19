package com.chpark.chcalendar.enumClass;

import lombok.Getter;

@Getter
public enum RequestType {
    REGISTER("회원가입", "register"),
    PASSWORD_RESET("비밀번호 초기화", "password_reset"),
    LOGIN("로그인", "login");

    private final String message;
    private final String code;

    RequestType(String message, String code) {
        this.message = message;
        this.code = code;
    }
}
