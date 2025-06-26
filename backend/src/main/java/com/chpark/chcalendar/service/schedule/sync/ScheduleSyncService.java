package com.chpark.chcalendar.service.schedule.sync;

import org.springframework.transaction.annotation.Transactional;

public interface ScheduleSyncService {

    @Transactional
    void syncSchedules(String accessToken, long userId);

}
