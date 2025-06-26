package com.chpark.chcalendar.service.calendar.sync;

import org.springframework.transaction.annotation.Transactional;

public interface CalendarSyncService {

    @Transactional
    void syncCalendars(String externalAccessToken, long userId);
}
