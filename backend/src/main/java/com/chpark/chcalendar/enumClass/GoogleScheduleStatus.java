package com.chpark.chcalendar.enumClass;

public enum GoogleScheduleStatus {
    CONFIRMED("confirmed"),
    TENTATIVE("tentative"),
    CANCELLED("cancelled");

    private final String value;
    GoogleScheduleStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // String -> Enum 변환 유틸
    public static GoogleScheduleStatus from(String value) {
        for (GoogleScheduleStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}


