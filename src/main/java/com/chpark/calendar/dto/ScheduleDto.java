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
    private Integer repeatId;

    public ScheduleDto(ScheduleEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.startAt = entity.getStartAt();
        this.endAt = entity.getEndAt();
        this.repeatId = entity.getRepeatId();
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
