package com.chpark.chcalendar.event.schedule;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record GoogleScheduleUpdateEvent (
        String title,
        String description,
        EventDateTime startAt,
        EventDateTime endAt,
        String calendarId,
        String scheduleId,
        String accessToken
) {
    public GoogleScheduleUpdateEvent(
            String title,
            String description,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String calendarId,
            String scheduleId,
            String accessToken
    ) {
        this(
                title,
                description,
                new EventDateTime()
                        .setDateTime(new DateTime(
                                startAt.atZone(ZoneId.of("Asia/Seoul"))
                                        .toInstant()
                                        .toEpochMilli()))
                        .setTimeZone("Asia/Seoul"),
                new EventDateTime()
                        .setDateTime(new DateTime(
                                endAt.atZone(ZoneId.of("Asia/Seoul"))
                                        .toInstant()
                                        .toEpochMilli()))
                        .setTimeZone("Asia/Seoul"),
                calendarId,
                scheduleId,
                accessToken
        );
    }
}