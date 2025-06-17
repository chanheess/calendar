package com.chpark.chcalendar.dto.calendar;

import com.chpark.chcalendar.enumClass.CalendarCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalendarSettingDto {
    @Setter
    Long calendarId;

    @Size(max = 10)
    String color;

    CalendarCategory category;

    Boolean checked;
}
