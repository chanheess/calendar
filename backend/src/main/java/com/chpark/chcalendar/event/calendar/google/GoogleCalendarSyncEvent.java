package com.chpark.chcalendar.event.calendar.google;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleCalendarSyncEvent {
    private String oauthAccessToken;
    private long userId;
}
