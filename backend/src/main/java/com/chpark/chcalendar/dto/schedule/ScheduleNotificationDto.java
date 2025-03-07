package com.chpark.chcalendar.dto.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleNotificationEntity;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class ScheduleNotificationDto {

    @NotNull
    @FutureOrPresent(message = "The notification date must be in the present or future")
    private LocalDateTime notificationAt;

    public ScheduleNotificationDto(LocalDateTime notificationAt) {
        this.notificationAt = notificationAt;
    }

    public ScheduleNotificationDto(ScheduleNotificationEntity entity) {
        this.notificationAt = entity.getNotificationAt();
    }

    public static List<ScheduleNotificationDto> fromScheduleNotificationEntityList(List<ScheduleNotificationEntity> entityList) {
        return entityList.stream()
                .map(ScheduleNotificationDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ScheduleNotificationDto.Response{"+
                " notificationAt='" + getNotificationAt() + '\'' +
                '}';
    }
}
