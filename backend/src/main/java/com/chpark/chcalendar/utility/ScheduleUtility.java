package com.chpark.chcalendar.utility;

import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.enumClass.ScheduleRepeatType;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.*;
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
            throw new IllegalArgumentException("잘못된 이메일 형식: " + email);
        }
    }

    public static ScheduleEntity parseScheduleEntity(JsonNode itemNode, CalendarEntity calendarEntity) {
        String id = itemNode.path("id").asText("");
        String title = itemNode.path("summary").asText("");
        String description = itemNode.path("description").asText("");
        LocalDateTime startAt = parseGoogleDateToKST(itemNode, "start");
        LocalDateTime endAt = parseGoogleDateToKST(itemNode, "end");
        String etag = itemNode.path("etag").asText("");
        LocalDateTime createdAt = parseToLocalDateTime(itemNode.path("created").asText(""));
        LocalDateTime updatedAt = parseToLocalDateTime(itemNode.path("updated").asText(""));

        return ScheduleEntity.builder()
                .title(title)
                .description(description)
                .startAt(startAt)
                .endAt(endAt)
                .userId(calendarEntity.getUserId())
                .calendarId(calendarEntity.getId())
                .providerId(id)
                .etag(etag)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static LocalDateTime parseGoogleDateToKST(JsonNode itemNode, String nodeName) {
        // dateTime 우선 시도
        String dateTimeStr = itemNode.path(nodeName).path("dateTime").asText("");
        if (dateTimeStr != null && !dateTimeStr.isEmpty()) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(dateTimeStr);
                // 항상 KST로 변환
                return odt.atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
            } catch (Exception e) {
                // 무시하고 아래로
            }
        }

        // 만약 dateTime 파싱이 실패하면 date(종일 일정) 시도
        String dateStr = itemNode.path(nodeName).path("date").asText("");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(dateStr);
                // KST로 00:00:00 변환
                return date.atStartOfDay(ZoneId.of("Asia/Seoul")).toLocalDateTime();
            } catch (Exception e) {
                // 무시
            }
        }

        // 아무것도 없으면 null
        return null;
    }

    public static LocalDateTime parseToLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return OffsetDateTime.parse(dateTimeStr).toLocalDateTime();
    }

    public static ScheduleEntity parseScheduleEntityFromGoogleEvent(Event event, CalendarEntity calendarEntity) {
        // ID, 제목, 설명
        String id = event.getId();
        String title = event.getSummary();
        String description = event.getDescription();

        // 시작/종료 시간 (DateTime 또는 Date)
        EventDateTime start = event.getStart();
        EventDateTime end = event.getEnd();
        LocalDateTime startAt = null;
        LocalDateTime endAt = null;
        if (start != null) {
            if (start.getDateTime() != null) {
                startAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(start.getDateTime().getValue()), ZoneId.of("Asia/Seoul"));
            } else if (start.getDate() != null) {
                LocalDate localDate = Instant.ofEpochMilli(start.getDate().getValue())
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toLocalDate();
                startAt = localDate.atStartOfDay();
            }
        }
        if (end != null) {
            if (end.getDateTime() != null) {
                endAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(end.getDateTime().getValue()), ZoneId.of("Asia/Seoul"));
            } else if (end.getDate() != null) {
                LocalDate localDate = Instant.ofEpochMilli(end.getDate().getValue())
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toLocalDate();
                endAt = localDate.atStartOfDay();
            }
        }

        // etag, 생성/수정 시간
        String etag = event.getEtag();
        LocalDateTime createdAt = null, updatedAt = null;
        if (event.getCreated() != null) {
            createdAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(event.getCreated().getValue()), ZoneId.of("Asia/Seoul"));
        }
        if (event.getUpdated() != null) {
            updatedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(event.getUpdated().getValue()), ZoneId.of("Asia/Seoul"));
        }

        return ScheduleEntity.builder()
                .title(title)
                .description(description)
                .startAt(startAt)
                .endAt(endAt)
                .userId(calendarEntity.getUserId())
                .calendarId(calendarEntity.getId())
                .providerId(id)
                .etag(etag)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

}
