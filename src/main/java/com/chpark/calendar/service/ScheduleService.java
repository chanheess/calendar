package com.chpark.calendar.service;

import com.chpark.calendar.domain.Schedule;
import com.chpark.calendar.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    /**
    * 스케쥴 생성
     */
    public Long createSchedule(Schedule schedule) {
        scheduleRepository.createSchedule(schedule);
        return schedule.getId();
    }

    /**
     * 모든 스케쥴 확인
     */
    public List<Schedule> findAllSchedule() {
        return scheduleRepository.findAll();
    }

}
