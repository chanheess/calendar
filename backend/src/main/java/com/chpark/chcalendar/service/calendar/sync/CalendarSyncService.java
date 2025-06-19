package com.chpark.chcalendar.service.calendar.sync;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public abstract class CalendarSyncService {

    @Transactional
    abstract public void syncCalendars(String externalAccessToken, HttpServletRequest request);
}
