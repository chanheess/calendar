package com.chpark.calendar.dto;

import com.chpark.calendar.entity.ScheduleEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleDto {

    private int id;
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public ScheduleDto(ScheduleEntity scheduleEntity) {
        setId(scheduleEntity.getId());
        setTitle(scheduleEntity.getTitle());
        setDescription(scheduleEntity.getDescription());
        setStartAt(scheduleEntity.getStartAt());
        setEndAt(scheduleEntity.getEndAt());
    }

    public static List<ScheduleDto> ConvertScheduleEntities(List<ScheduleEntity> scheduleEntities) {
        return scheduleEntities.stream()
                .map(ScheduleDto::new)
                .collect(Collectors.toList());
    }

}
