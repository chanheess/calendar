package com.chpark.chcalendar.event.calendar.google;

import com.chpark.chcalendar.service.calendar.sync.GoogleCalendarSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GoogleCalendarSyncListener {

    private final GoogleCalendarSyncService calendarSyncService;

    @Async
    @EventListener
    public void handleSyncCalendars(GoogleCalendarSyncEvent event) {
        calendarSyncService.syncCalendars(event.getOauthAccessToken(), event.getUserId());
    }
}
