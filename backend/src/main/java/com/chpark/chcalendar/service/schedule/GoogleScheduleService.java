package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.event.schedule.GoogleScheduleCreateEvent;
import com.chpark.chcalendar.repository.schedule.ScheduleNotificationRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleQueryRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepeatRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.service.calendar.CalendarService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class GoogleScheduleService extends ScheduleService {
    public GoogleScheduleService(ScheduleRepository scheduleRepository, ScheduleNotificationRepository scheduleNotificationRepository, ScheduleRepeatRepository scheduleRepeatRepository, ScheduleQueryRepository scheduleQueryRepository, ScheduleRepeatService scheduleRepeatService, ScheduleNotificationService scheduleNotificationService, ScheduleGroupService scheduleGroupService, Map<CalendarCategory, CalendarService> calendarServiceMap, ApplicationEventPublisher eventPublisher) {
        super(scheduleRepository, scheduleNotificationRepository, scheduleRepeatRepository, scheduleQueryRepository, scheduleRepeatService, scheduleNotificationService, scheduleGroupService, calendarServiceMap);

        this.eventPublisher = eventPublisher;
    }

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ScheduleDto.Response createByForm(ScheduleDto.Request scheduleDto, long userId, String accessToken, String CalendarId) {
        ScheduleDto.Response result = super.createByForm(scheduleDto, userId);
        ScheduleDto localSchedule = result.getScheduleDto();

        eventPublisher.publishEvent(new GoogleScheduleCreateEvent(
                        localSchedule.getTitle(),
                        localSchedule.getDescription(),
                        localSchedule.getStartAt(),
                        localSchedule.getEndAt(),
                        localSchedule.getId(),
                        CalendarId,
                        accessToken)
        );

        return result;
    }

}
