package com.chpark.chcalendar.dto.calendar;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CalendarColorDto {
    @NotNull
    long calendarId;

    @Size(max = 20)
    String color;
}
