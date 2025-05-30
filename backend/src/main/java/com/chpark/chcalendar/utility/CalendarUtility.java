package com.chpark.chcalendar.utility;

import com.chpark.chcalendar.exception.ScheduleException;
import com.chpark.chcalendar.exception.authentication.CalendarAuthenticationException;
import com.chpark.chcalendar.exception.authentication.GroupAuthenticationException;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import com.chpark.chcalendar.service.schedule.ScheduleGroupService;
import com.chpark.chcalendar.service.user.GroupUserService;
import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class CalendarUtility {

    private CalendarUtility() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<Long> getUserCalendars(long userId, GroupUserService groupUserService, UserCalendarService userCalendarService) {

        List<Long> resultList = new ArrayList<>();

        resultList.addAll(groupUserService.findMyGroupsId(userId));
        resultList.addAll(userCalendarService.findCalendarIdList(userId));

        return resultList;
    }

    public static void checkCalendarAuthority(long userId, long createdUserId, long calendarId, Long scheduleId, GroupUserService groupUserService, UserCalendarService userCalendarService, ScheduleGroupService scheduleGroupService) {

        int checkCount = 0;

        try {
            groupUserService.getGroupUser(userId, calendarId);
        } catch (GroupAuthenticationException ex) {
            checkCount++;
        }
        try {
            userCalendarService.checkCalendarAdminUser(calendarId, userId);
        }
        catch (EntityNotFoundException ex) {
            checkCount++;
        }

        try {
            scheduleGroupService.checkScheduleGroupAuth(userId, createdUserId, scheduleId);
        } catch (ScheduleException ex) {
            checkCount++;
        }

        if (checkCount == 3) {
            throw new CalendarAuthenticationException("You do not have permission.");
        }
    }

    public static List<Long> getAuthorizedCalendars(long userId, List<Long> calendarIdList, GroupUserService groupUserService, UserCalendarService userCalendarService) {

        List<Long> resultList = new ArrayList<>(calendarIdList);

        resultList.retainAll(groupUserService.findMyGroupsId(userId));
        calendarIdList.retainAll(userCalendarService.findCalendarIdList(userId));

        resultList.addAll(calendarIdList);

        return resultList;
    }
}
