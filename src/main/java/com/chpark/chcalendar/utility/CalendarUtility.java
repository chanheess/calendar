package com.chpark.chcalendar.utility;

import com.chpark.chcalendar.exception.authority.CalendarAuthorityException;
import com.chpark.chcalendar.exception.authority.GroupAuthorityException;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import com.chpark.chcalendar.service.group.GroupUserService;
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

    public static void checkCalendarAuthority(long userId, long calendarId, GroupUserService groupUserService, UserCalendarService userCalendarService) {

        int checkCount = 0;

        try {
            groupUserService.checkGroupUser(userId, calendarId);
        } catch (GroupAuthorityException ex) {
            checkCount++;
        }
        try {
            userCalendarService.checkCalendarAdminUser(calendarId, userId);
        }
        catch (EntityNotFoundException ex) {
            checkCount++;
        }

        if(checkCount == 2) {
            throw new CalendarAuthorityException("권한이 없습니다.");
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
