package com.chpark.chcalendar.utility;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.enumClass.CRUDAction;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.exception.ScheduleException;
import com.chpark.chcalendar.exception.authorization.CalendarAuthorizationException;
import com.chpark.chcalendar.exception.authorization.GroupAuthorizationException;
import com.chpark.chcalendar.service.calendar.CalendarService;
import com.chpark.chcalendar.service.schedule.ScheduleGroupService;
import jakarta.persistence.EntityNotFoundException;

import java.util.*;

public class CalendarUtility {

    private CalendarUtility() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<Long> getUserCalendar(long userId, CalendarService calendarService) {
        return calendarService.findCalendarIdList(userId);
    }

    public static void deleteCalendarAccount(long userId, Map<CalendarCategory, CalendarService> calendarServiceMap) {
        calendarServiceMap.forEach((category, calendar) -> {
            List<CalendarDto.Response> calendarList = calendar.findCalendarList(userId);

            calendarList.forEach(targetCalendar -> {
                calendarServiceMap.get(targetCalendar.getCategory()).deleteCalendar(userId, targetCalendar.getId());
            });
        });
    }

    public static List<Long> getUserAllCalendar(long userId, List<CalendarService> calendarServiceList) {
        List<Long> result = new ArrayList<>();

        calendarServiceList.forEach( calendarService ->
                result.addAll(getUserCalendar(userId, calendarService))
        );

        return result;
    }

    public static void checkCalendarAuthority(CRUDAction action, long userId, long createdUserId, long calendarId, Long scheduleId, List<CalendarService> calendarServiceList, ScheduleGroupService scheduleGroupService) {
        int checkCount = 0;

        for (CalendarService calendarService : calendarServiceList) {
            try {
                calendarService.checkAuthority(action, userId, calendarId);
            } catch (GroupAuthorizationException | EntityNotFoundException ex) {
                checkCount++;
            }
        }

        try {
            scheduleGroupService.checkScheduleGroupAuth(userId, createdUserId, scheduleId);
        } catch (ScheduleException ex) {
            checkCount++;
        }

        if (checkCount == calendarServiceList.size() + 1) {
            throw new CalendarAuthorizationException("You do not have permission.");
        }
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
