package com.chpark.chcalendar.dto.calendar;

import com.chpark.chcalendar.enumClass.CalendarCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CalendarColorDto {
    Long calendarId;

    @NotNull
    @Size(max = 20)
    String color;

    @NotNull
    CalendarCategory category;


    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }
}
