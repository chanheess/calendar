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


    public ScheduleDto(ScheduleEntity entity) {
        setId(entity.getId());
        setTitle(entity.getTitle());
        setDescription(entity.getDescription());
        setStartAt(entity.getStartAt());
        setEndAt(entity.getEndAt());
    }

    public static List<ScheduleDto> fromScheduleEntityList(List<ScheduleEntity> entityList) {
        return entityList.stream()
                .map(ScheduleDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ScheduleDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                '}';
    }

}
