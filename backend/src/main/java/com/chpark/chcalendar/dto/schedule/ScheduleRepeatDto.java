package com.chpark.chcalendar.dto.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleRepeatEntity;
import com.chpark.chcalendar.enumClass.ScheduleRepeatType;
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
    private int repeatInterval;

    @NotNull
    private ScheduleRepeatType repeatType;

    @FutureOrPresent(message = "The repeat date must be in the present or future")
    private LocalDateTime endAt;

    public ScheduleRepeatDto(ScheduleRepeatEntity repeatEntity) {
        this.repeatInterval = repeatEntity.getRepeatInterval();
        this.repeatType = repeatEntity.getRepeatType();
        this.endAt = repeatEntity.getEndAt();
    }
}

