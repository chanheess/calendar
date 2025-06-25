package com.chpark.chcalendar.service.schedule.sync;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

public interface ScheduleSyncService {

    @Transactional
    void syncSchedules(String accessToken, HttpServletRequest request);

}
