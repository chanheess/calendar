package com.chpark.chcalendar.event.schedule.google;

public record GoogleScheduleDeleteEvent (
    String calendarId,
    String scheduleId,
    String accessToken
) {}
