package com.chpark.chcalendar.enumClass;

import lombok.Getter;

@Getter
public enum ScheduleRepeatType {
    DAY("d"),
    WEEK("w"),
    MONTH("m"),
    YEAR("y");

    private final String code;

    ScheduleRepeatType(String code) {
        this.code = code;
    }
}

