package com.chpark.chcalendar.event.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScheduleDeleteEvent {
    long userId;
    long calendarId;
}
