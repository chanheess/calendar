package com.chpark.chcalendar.entity.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ScheduleBatchEntity {
    private Map<Long, ScheduleEntity> schedules;
    private List<ScheduleNotificationEntity> notifications;
}
