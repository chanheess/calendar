package com.chpark.calendar.enumClass;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ScheduleRepeatScope {
    CURRENT("current-only"),        // 현재 일정만 수정
    FUTURE("current-and-future");    // 이후 일정까지 모두 수정

    private final String value;

    ScheduleRepeatScope(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static ScheduleRepeatScope fromValue(String value) {
        for (ScheduleRepeatScope scope : ScheduleRepeatScope.values()) {
            if (scope.value.equalsIgnoreCase(value)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Invalid value for ScheduleRepeatScope: " + value);
    }
}

