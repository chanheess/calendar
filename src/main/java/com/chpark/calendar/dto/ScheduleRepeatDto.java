package com.chpark.calendar.dto;

import com.chpark.calendar.entity.ScheduleRepeatEntity;
import com.chpark.calendar.enumClass.ScheduleRepeatType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class ScheduleRepeatDto {
    @NotNull
    private ScheduleRepeatType repeatType;

    @NotNull
    private int repeatInterval;

    @FutureOrPresent(message = "The notification date must be in the present or future")
    private LocalDateTime endAt;

    public ScheduleRepeatDto(ScheduleRepeatEntity repeatEntity) {
        this.repeatType = repeatEntity.getRepeatType();
        this.repeatInterval = repeatEntity.getRepeatInterval();
        this.endAt = repeatEntity.getEndAt();
    }
}

