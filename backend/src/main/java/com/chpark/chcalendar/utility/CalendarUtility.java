package com.chpark.chcalendar.utility;

import com.chpark.chcalendar.service.calendar.CalendarService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalendarUtility {

    private CalendarUtility() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<Long> getUserCalendar(long userId, CalendarService calendarService) {
        return calendarService.findCalendarIdList(userId);
    }

    public static List<Long> getUserAllCalendar(long userId, List<CalendarService> calendarServiceList) {
        List<Long> result = new ArrayList<>();

        calendarServiceList.forEach( calendarService ->
                result.addAll(getUserCalendar(userId, calendarService))
        );

        return result;
    }

    public static List<Long> getAuthorizedCalendars(long userId, List<Long> calendarIdList, List<CalendarService> calendarServiceList) {
        Set<Long> resultList = new HashSet<>(calendarIdList);

        for (CalendarService calendarService : calendarServiceList) {
            List<Long> authorizedIds = calendarService.findCalendarIdList(userId);
            resultList.retainAll(authorizedIds); // AND 조건(교집합)만 남김
        }

        return resultList.stream().toList();
    }
}
