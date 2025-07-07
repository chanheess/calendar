package com.chpark.chcalendar.enumClass;

import lombok.Getter;

@Getter
public enum CalendarCategory {
    USER("개인"),
    GROUP("그룹"),
    GOOGLE("구글");

    private final String message;

    CalendarCategory(String message) {
        this.message = message;
    }

    public boolean isExternalProvider() {
        return this.ordinal() >= 2;
    }
}
