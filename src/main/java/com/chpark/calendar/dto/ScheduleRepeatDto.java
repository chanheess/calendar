package com.chpark.calendar.dto;

import com.chpark.calendar.entity.ScheduleRepeatEntity;
import com.chpark.calendar.enumClass.ScheduleRepeatType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleRepeatDto {

    @NotNull
    private ScheduleRepeatType repeatType;

    @NotNull
    private int repeatInterval;

    @NotNull
    @FutureOrPresent(message = "The notification date must be in the present or future")
    private LocalDateTime endAt;

    public ScheduleRepeatDto(ScheduleRepeatEntity repeatEntity) {
        this.repeatType = repeatEntity.getRepeatType();
        this.repeatInterval = repeatEntity.getRepeatInterval();
        this.endAt = repeatEntity.getEndAt();
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

