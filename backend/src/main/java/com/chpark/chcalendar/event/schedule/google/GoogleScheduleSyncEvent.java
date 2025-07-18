package com.chpark.chcalendar.event.schedule.google;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleScheduleSyncEvent {
    private String oauthAccessToken;
    private long userId;
}
