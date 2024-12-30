package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;

import java.util.List;


public interface CalendarService {
    CalendarInfoDto create(long userId, String title);
    List<CalendarInfoDto> findCalendarList(long userId);
}
