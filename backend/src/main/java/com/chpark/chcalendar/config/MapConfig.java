package com.chpark.chcalendar.config;

import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.NotificationCategory;
import com.chpark.chcalendar.service.calendar.CalendarService;
import com.chpark.chcalendar.service.calendar.sync.CalendarSyncService;
import com.chpark.chcalendar.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MapConfig {

    @Bean
    public Map<CalendarCategory, CalendarService> calendarServiceMap(
            @Qualifier("userCalendarService") CalendarService userCalendarService,
            @Qualifier("groupCalendarService") CalendarService groupCalendarService,
            @Qualifier("googleCalendarService") CalendarService googleCalendarService) {
        Map<CalendarCategory, CalendarService> serviceMap = new HashMap<>();
        serviceMap.put(CalendarCategory.USER, userCalendarService);
        serviceMap.put(CalendarCategory.GROUP, groupCalendarService);
        serviceMap.put(CalendarCategory.GOOGLE, googleCalendarService);
        return serviceMap;
    }

    @Bean
    public Map<NotificationCategory, NotificationService> notificationServiceMap(
            @Qualifier("notificationScheduleService") NotificationService notificationScheduleService,
            @Qualifier("notificationGroupService") NotificationService notificationGroupService) {
        Map<NotificationCategory, NotificationService> serviceMap = new HashMap<>();
        serviceMap.put(NotificationCategory.SCHEDULE, notificationScheduleService);
        serviceMap.put(NotificationCategory.GROUP, notificationGroupService);
        return serviceMap;
    }

    @Bean
    public Map<CalendarCategory, CalendarSyncService> calendarSyncServiceMap(
            @Qualifier("googleCalendarSyncService") CalendarSyncService googleCalendarSyncService // 이름 주의!
    ) {
        Map<CalendarCategory, CalendarSyncService> serviceMap = new HashMap<>();
        serviceMap.put(CalendarCategory.GOOGLE, googleCalendarSyncService);
        return serviceMap;
    }
}
