package com.chpark.chcalendar.dto.notification;

import com.chpark.chcalendar.dto.schedule.ScheduleGroupDto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NotificationScheduleDto {

    long groupId;

    @NotNull
    List<ScheduleGroupDto> scheduleGroupDto;

}
