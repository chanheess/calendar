package com.chpark.calendar.repository;

import com.chpark.calendar.domain.Schedule;

import java.util.List;

public interface ScheduleRepository {
    Schedule createSchedule(Schedule schedule);
    List<Schedule> findAll();
}
