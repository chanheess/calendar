package com.chpark.chcalendar.event.schedule.google;

import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.chpark.chcalendar.utility.ScheduleUtility.parseLocalNotificationToGoogleNotification;

public record GoogleScheduleCreateEvent(
        String title,
        String description,
        EventDateTime startAt,
        EventDateTime endAt,
        Long localScheduleId,
        String calendarId,
        Event.Reminders reminders,
        String accessToken
) {
    public GoogleScheduleCreateEvent(
            String title,
            String description,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long localScheduleId,
            String calendarId,
            List<ScheduleNotificationDto> notificationList,
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
                parseLocalNotificationToGoogleNotification(startAt, notificationList),
                accessToken
        );
    }

    public GoogleScheduleCreateEvent(String title, String description, EventDateTime startAt, EventDateTime endAt, Long localScheduleId, String calendarId, Event.Reminders reminders, String accessToken) {
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.localScheduleId = localScheduleId;
        this.calendarId = calendarId;
        this.reminders = reminders;
        this.accessToken = accessToken;
    }
}