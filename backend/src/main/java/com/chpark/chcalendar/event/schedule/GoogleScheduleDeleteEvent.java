package com.chpark.chcalendar.event.schedule;

public record GoogleScheduleDeleteEvent (
    String calendarId,
    String scheduleId,
    String accessToken
) {}
