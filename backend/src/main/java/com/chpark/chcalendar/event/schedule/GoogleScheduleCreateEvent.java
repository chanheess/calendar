package com.chpark.chcalendar.event.schedule;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record GoogleScheduleCreateEvent(
        String title,
        String description,
        EventDateTime startAt,
        EventDateTime endAt,
        Long localScheduleId,
        String calendarId,
        String accessToken
) {
    public GoogleScheduleCreateEvent(
            String title,
            String description,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long localScheduleId,
            String calendarId,
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
                localScheduleId,
                calendarId,
                accessToken
        );
    }
}