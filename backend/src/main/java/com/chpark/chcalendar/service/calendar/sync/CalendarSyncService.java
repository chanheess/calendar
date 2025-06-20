package com.chpark.chcalendar.service.calendar.sync;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

public interface CalendarSyncService {

    @Transactional
    void syncCalendars(String externalAccessToken, HttpServletRequest request);
}
