package com.chpark.chcalendar.config;

import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.service.calendar.CalendarService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CalendarServiceConfig {

    @Bean
    public Map<CalendarCategory, CalendarService> calendarServiceMap(
            @Qualifier("userCalendarService") CalendarService userCalendarService,
            @Qualifier("groupCalendarService") CalendarService groupCalendarService) {
        Map<CalendarCategory, CalendarService> serviceMap = new HashMap<>();
        serviceMap.put(CalendarCategory.USER, userCalendarService);
        serviceMap.put(CalendarCategory.GROUP, groupCalendarService);
        return serviceMap;
    }
}
