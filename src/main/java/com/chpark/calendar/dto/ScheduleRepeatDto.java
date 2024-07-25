package com.chpark.calendar.dto;

import com.chpark.calendar.entity.ScheduleRepeatEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleRepeatDto {

    private String repeatType;
    private int repeatInterval;
    private LocalDateTime endAt;

    public ScheduleRepeatDto(String repeatType, int repeatInterval, LocalDateTime endAt) {
        this.repeatType = repeatType;
        this.repeatInterval = repeatInterval;
        this.endAt = endAt;
    }

    @Getter
    @NoArgsConstructor
    public static class Response extends ScheduleRepeatDto{

        private int scheduleId;

        public Response(ScheduleRepeatEntity repeatEntity) {
            super(repeatEntity.getRepeatType(), repeatEntity.getRepeatInterval(), repeatEntity.getEndAt());
            this.scheduleId = repeatEntity.getScheduleId();
        }
    }
}

