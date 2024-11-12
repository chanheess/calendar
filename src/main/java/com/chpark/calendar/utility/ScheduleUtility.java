package com.chpark.calendar.utility;

import com.chpark.calendar.enumClass.ScheduleRepeatType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ScheduleUtility {

    // 인스턴스 생성 방지
    private ScheduleUtility() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int calculateRepeatCount(LocalDateTime startDate, LocalDateTime endDate, int interval, ScheduleRepeatType repeatType) {
        int repeatCount = 500;

        if(endDate == null) {
            return repeatCount;
        }

        return switch (repeatType) {
            case d -> (int) ChronoUnit.DAYS.between(startDate, endDate) / interval;
            case w -> (int) ChronoUnit.WEEKS.between(startDate, endDate) / interval;
            case m -> (int) ChronoUnit.MONTHS.between(startDate, endDate) / interval;
            case y -> (int) ChronoUnit.YEARS.between(startDate, endDate) / interval;
        };
    }

    /**
     * 반복 타입에 따른 추가된 일자를 계산합니다.
     *
     * @param date 시작 일자
     * @param repeatType 반복 타입
     * @param repeatInterval 반복 간격
     * @return 반복 타입에 따른 추가된 일자
     */
    public static LocalDateTime calculateRepeatPlusDate(LocalDateTime date, ScheduleRepeatType repeatType, int repeatInterval) {
        if(date == null) {
            throw new IllegalArgumentException("The date parameters must not be null");
        }
        if(repeatInterval < 1) {
            return date;
        }

        return switch (repeatType) {
            case d -> date.plusDays(repeatInterval);
            case w -> date.plusWeeks(repeatInterval);
            case m -> date.plusMonths(repeatInterval);
            case y -> date.plusYears(repeatInterval);
        };
    }
}
