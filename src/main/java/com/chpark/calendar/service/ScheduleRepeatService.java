package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleRepeatDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.entity.ScheduleRepeatEntity;
import com.chpark.calendar.exception.CustomException;
import com.chpark.calendar.repository.ScheduleRepeatRepository;
import com.chpark.calendar.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ScheduleRepeatService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleRepeatRepository scheduleRepeatRepository;

    @Transactional
    public ScheduleRepeatDto.Response create(int scheduleId, ScheduleRepeatDto repeatDto) {

        Optional<ScheduleEntity> scheduleEntity = scheduleRepository.findById(scheduleId);
        if (scheduleEntity.isEmpty()) {
            throw new CustomException("Schedule not found");
        }

        ScheduleRepeatEntity repeatEntity = new ScheduleRepeatEntity(scheduleId, repeatDto);
        ScheduleRepeatEntity createEntity = scheduleRepeatRepository.save(repeatEntity);
        //1:1관계 저장
        scheduleEntity.get().setRepeat(createEntity);
        scheduleRepository.save(scheduleEntity.get());

        return new ScheduleRepeatDto.Response(createEntity);
    }


}
