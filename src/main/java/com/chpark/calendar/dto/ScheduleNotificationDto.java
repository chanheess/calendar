package com.chpark.calendar.dto;

import com.chpark.calendar.entity.ScheduleNotificationEntity;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleNotificationDto {

    @Getter
    @NoArgsConstructor
    public static class Request {
        //TODO: 커스텀 메시지가 나오도록 수정
        @FutureOrPresent(message = "The notification date must be in the present or future")
        private LocalDateTime notificationAt;

        public Request(ScheduleNotificationEntity entity) {
            this.notificationAt = entity.getNotificationAt();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response {

        private int id;
        private int scheduleId;

        @FutureOrPresent(message = "The notification date must be in the present or future")
        private LocalDateTime notificationAt;

        public Response(ScheduleNotificationEntity entity) {
            setId(entity.getId());
            setScheduleId(entity.getScheduleId());
            setNotificationAt(entity.getNotificationAt());
        }

        public static List<Response> fromScheduleNotificationEntityList(List<ScheduleNotificationEntity> entityList) {
            return entityList.stream()
                    .map(Response::new)
                    .collect(Collectors.toList());
        }
    }
}
