package com.chpark.calendar.dto;

import com.chpark.calendar.entity.ScheduleNotificationEntity;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class ScheduleNotificationDto {

    @FutureOrPresent(message = "The notification date must be in the present or future")
    private LocalDateTime notificationAt;

    public ScheduleNotificationDto(LocalDateTime dateAt) {
        this.notificationAt = dateAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response extends ScheduleNotificationDto {

        private int id;

        @NotNull
        private int scheduleId;

        public Response(ScheduleNotificationEntity entity) {
            super(entity.getNotificationAt());
            setId(entity.getId());
            setScheduleId(entity.getScheduleId());
        }

        public static List<Response> fromScheduleNotificationEntityList(List<ScheduleNotificationEntity> entityList) {
            return entityList.stream()
                    .map(Response::new)
                    .collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return "ScheduleNotificationDto.Response{" +
                    "id=" + id +
                    ", scheduleId=" + scheduleId +
                    ", notificationAt='" + getNotificationAt() + '\'' +
                    '}';
        }
    }
}
