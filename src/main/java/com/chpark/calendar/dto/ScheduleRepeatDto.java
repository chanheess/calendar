package com.chpark.calendar.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleRepeatDto {

    private String repeatType;
    private int repeatInterval;
    private LocalDateTime endAt;

    @Getter
    @NoArgsConstructor
    public static class Response extends ScheduleRepeatDto{

        private int scheduleId;
    }
}

