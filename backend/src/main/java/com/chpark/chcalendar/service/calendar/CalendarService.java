package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarColorDto;
import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


public interface CalendarService {
    CalendarInfoDto.Response create(long userId, String title);
    List<CalendarInfoDto.Response> findCalendarList(HttpServletRequest request);
    CalendarColorDto changeColor(long userId, CalendarColorDto calendarColorDto);
}
