package com.chpark.chcalendar.dto.notification;

import com.chpark.chcalendar.dto.schedule.ScheduleGroupDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationScheduleDto {

    long calendarId;

    @NotNull
    List<ScheduleGroupDto> scheduleGroupDto;

}
