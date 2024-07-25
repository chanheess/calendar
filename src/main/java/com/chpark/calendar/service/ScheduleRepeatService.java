package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleRepeatDto;
import com.chpark.calendar.repository.ScheduleRepeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class ScheduleRepeatService {

    private final ScheduleRepeatRepository scheduleRepeatRepository;

    public void create() {
        ScheduleRepeatDto s;
    }
}
