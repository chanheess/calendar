package com.chpark.calendar.service;

import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.repository.CalendarRepository;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public CalendarService(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    public int create(ScheduleEntity scheduleEntity) {
        calendarRepository.save(scheduleEntity);
        return scheduleEntity.getId();
    }

    //public

}
