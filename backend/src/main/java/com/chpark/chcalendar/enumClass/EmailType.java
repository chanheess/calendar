package com.chpark.chcalendar.enumClass;

import lombok.Getter;

@Getter
public enum EmailType {
    REGISTER("회원가입"),
    PASSWORD_RESET("비밀번호 초기화");

    private final String message;

    EmailType(String message) {
        this.message = message;
    }
}
