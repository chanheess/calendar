package com.chpark.chcalendar.dto.schedule;

import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.CRUDAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ScheduleTargetActionDto {
    CalendarCategory category;
    CRUDAction action;
    String calendarProviderId;
    String scheduleProviderId;
    String accessToken;
}
