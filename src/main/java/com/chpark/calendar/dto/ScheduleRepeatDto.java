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

    public ScheduleRepeatDto(ScheduleRepeatEntity repeatEntity) {
        this.repeatType = repeatEntity.getRepeatType();
        this.repeatInterval = repeatEntity.getRepeatInterval();
        this.endAt = repeatEntity.getEndAt();
    }

    public ScheduleRepeatDto(ScheduleRepeatDto repeatDto) {
        this.repeatType = repeatDto.getRepeatType();
        this.repeatInterval = repeatDto.getRepeatInterval();
        this.endAt = repeatDto.getEndAt();
    }

    @Getter
    @NoArgsConstructor
    public static class Response extends ScheduleRepeatDto{

        private int id;

        public Response(ScheduleRepeatEntity repeatEntity) {
            super(repeatEntity);
            this.id = repeatEntity.getId();
        }
    }
}

