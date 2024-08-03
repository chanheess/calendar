package com.chpark.calendar.dto;

import com.chpark.calendar.entity.ScheduleRepeatEntity;
import com.chpark.calendar.enumClass.ScheduleRepeatType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleRepeatDto {

    private ScheduleRepeatType repeatType;
    private int repeatInterval;
    private LocalDateTime endAt;

    public ScheduleRepeatDto(ScheduleRepeatType repeatType, int repeatInterval, LocalDateTime endAt) {
        this.repeatType = repeatType;
        this.repeatInterval = repeatInterval;
        this.endAt = endAt;
    }

    @Getter
    @NoArgsConstructor
    public static class Response extends ScheduleRepeatDto{

        private int id;

        public Response(ScheduleRepeatEntity repeatEntity) {
            super(repeatEntity.getRepeatType(), repeatEntity.getRepeatInterval(), repeatEntity.getEndAt());
            this.id = repeatEntity.getId();
        }
    }
}

