package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarColorDto;
import com.chpark.chcalendar.dto.calendar.CalendarDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


public interface CalendarService {
    CalendarDto.Response create(long userId, String title);
    List<CalendarDto.Response> findCalendarList(HttpServletRequest request);
    CalendarColorDto changeColor(long userId, CalendarColorDto calendarColorDto);
}
