package com.chpark.chcalendar.enumClass;

import lombok.Getter;

@Getter
public enum GoogleScheduleStatus {
    CONFIRMED("confirmed"),
    TENTATIVE("tentative"),
    CANCELLED("cancelled");

    private final String value;

    GoogleScheduleStatus(String value) {
        this.value = value;
    }

    public static GoogleScheduleStatus from(String value) {
        for (GoogleScheduleStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}