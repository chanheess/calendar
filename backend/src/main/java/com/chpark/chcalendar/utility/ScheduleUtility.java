package com.chpark.chcalendar.utility;

import com.chpark.chcalendar.enumClass.ScheduleRepeatType;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

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
            case DAY -> (int) ChronoUnit.DAYS.between(startDate, endDate) / interval;
            case WEEK -> (int) ChronoUnit.WEEKS.between(startDate, endDate) / interval;
            case MONTH -> (int) ChronoUnit.MONTHS.between(startDate, endDate) / interval;
            case YEAR -> (int) ChronoUnit.YEARS.between(startDate, endDate) / interval;
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
            case DAY -> date.plusDays(repeatInterval);
            case WEEK -> date.plusWeeks(repeatInterval);
            case MONTH -> date.plusMonths(repeatInterval);
            case YEAR -> date.plusYears(repeatInterval);
        };
    }

    public static String formatNotificationDate(LocalDateTime dateTime, LocalDateTime noticiationTime) {

        long days = ChronoUnit.DAYS.between(noticiationTime, dateTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일, a h:mm분", Locale.KOREAN);
        String formattedDate = dateTime.format(formatter);

        if (days > 0) {
            return days + "일 전 " + formattedDate;
        } else {
            long hours = ChronoUnit.HOURS.between(noticiationTime, dateTime);
            if (hours > 0) {
                return hours + "시간 전 " + formattedDate;
            } else {
                long minutes = ChronoUnit.MINUTES.between(noticiationTime, dateTime);
                if (minutes > 0) {
                    return minutes + "분 전 " + formattedDate;
                } else {
                    return "방금 " + formattedDate;
                }
            }
        }
    }

    public static void validateEmail(String email) {
        EmailValidator validator = EmailValidator.getInstance();
        if (!validator.isValid(email)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

}
