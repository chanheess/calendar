package com.chpark.calendar.utility;

import com.chpark.calendar.enumClass.ScheduleRepeatType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ScheduleUtility {

    // 인스턴스 생성 방지
    private ScheduleUtility() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 일정 반복 횟수를 계산합니다.
     *
     * @param startDate 시작 일자
     * @param endDate 끝 일자
     * @param interval 반복 간격
     * @param repeatType 반복 형식 (매일, 매주, 매월, 매년)
     * @return 두 일자 사이의 반복 횟수
    */
    public static int calculateRepeatCount(LocalDateTime startDate, LocalDateTime endDate, int interval, ScheduleRepeatType repeatType) {
        int repeatCount = 500;

        if(endDate == null) {
            return repeatCount;
        }

        switch (repeatType) {
            case d:
                return (int) ChronoUnit.DAYS.between(startDate, endDate) / interval;
            case w:
                return (int) ChronoUnit.WEEKS.between(startDate, endDate) / interval;
            case m:
                return (int) ChronoUnit.MONTHS.between(startDate, endDate) / interval;
            case y:
                return (int) ChronoUnit.YEARS.between(startDate, endDate) / interval;
            default:
                throw new IllegalArgumentException("Unknown interval type: " + interval);
        }
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
        if(date == null || repeatInterval < 1) {
            throw new IllegalArgumentException("The date parameters must not be null, and repeatInterval must be greater than zero.");
        }

        switch (repeatType) {
            case d:
                return date.plusDays(repeatInterval);
            case w:
                return date.plusWeeks(repeatInterval);
            case m:
                return date.plusMonths(repeatInterval);
            case y:
                return date.plusYears(repeatInterval);
            default:
                throw new IllegalArgumentException("Unknown interval type: " + repeatType);
        }
    }

}
