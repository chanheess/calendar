package com.chpark.chcalendar.event.schedule;

import com.chpark.chcalendar.service.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class ScheduleEventListener {

    private final ScheduleService scheduleService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleDelete(ScheduleDeleteEvent scheduleDeleteEvent) {
        scheduleService.deleteAccount(scheduleDeleteEvent.getUserId(), scheduleDeleteEvent.getCalendarId());
    }
}
