package com.chpark.chcalendar.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarListDto {

    List<CalendarInfoDto.Response> groupInfo;
    List<CalendarInfoDto.Response> calendarInfo;
}
