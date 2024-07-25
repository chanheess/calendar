package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleRepeatDto;
import com.chpark.calendar.entity.ScheduleRepeatEntity;
import com.chpark.calendar.repository.ScheduleRepeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ScheduleRepeatService {

    private final ScheduleRepeatRepository scheduleRepeatRepository;

//    public Optional<ScheduleRepeatDto.Response> create(int scheduleId, ScheduleRepeatDto repeatDto) {
//        ScheduleRepeatEntity repeatEntity = new ScheduleRepeatEntity(scheduleId, repeatDto);
//        Optional<ScheduleRepeatEntity> createEntity = Optional.of(scheduleRepeatRepository.save(repeatEntity));
//        Optional<ScheduleRepeatDto.Response> = createEntity;
//
//
//    }
}
