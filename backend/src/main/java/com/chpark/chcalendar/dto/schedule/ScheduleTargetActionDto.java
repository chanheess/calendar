package com.chpark.chcalendar.dto.schedule;

import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.CRUDAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ScheduleTargetActionDto {
    private CalendarCategory category;
    private CRUDAction action;
    private String calendarProviderId;
    private String scheduleProviderId;
    private String accessToken;
    
    @Override
    public String toString() {
        return "ScheduleTargetActionDto{" +
                "category=" + category +
                ", action=" + action +
                ", calendarProviderId='" + calendarProviderId + '\'' +
                ", scheduleProviderId='" + scheduleProviderId + '\'' +
                ", accessToken='***'" +
                '}';
    }
}
