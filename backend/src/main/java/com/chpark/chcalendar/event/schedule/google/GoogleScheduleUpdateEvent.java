package com.chpark.chcalendar.event.schedule.google;

import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.chpark.chcalendar.utility.ScheduleUtility.parseLocalNotificationToGoogleNotification;

public record GoogleScheduleUpdateEvent (
        String title,
        String description,
        EventDateTime startAt,
        EventDateTime endAt,
        String calendarId,
        String scheduleId,
        Event.Reminders reminders,
        String accessToken
) {
    public GoogleScheduleUpdateEvent(
            String title,
            String description,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String calendarId,
            String scheduleId,
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
                calendarId,
                scheduleId,
                parseLocalNotificationToGoogleNotification(startAt, notificationList),
                accessToken
        );
    }
}