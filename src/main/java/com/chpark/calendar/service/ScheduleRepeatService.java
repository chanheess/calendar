package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleRepeatDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.entity.ScheduleRepeatEntity;
import com.chpark.calendar.exception.CustomException;
import com.chpark.calendar.repository.schedule.ScheduleBatchRepository;
import com.chpark.calendar.repository.schedule.ScheduleRepeatRepository;
import com.chpark.calendar.repository.schedule.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ScheduleRepeatService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleRepeatRepository scheduleRepeatRepository;
    private final ScheduleBatchRepository scheduleBatchRepository;

    @Transactional
    public ScheduleRepeatDto create(int scheduleId, ScheduleRepeatDto repeatDto) {

        if(repeatDto == null) {
            throw new CustomException("repeat");
        }

        //기준 일정 가져오기
        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with id: " + scheduleId)
        );

        //이미 반복이 만들어져있다면 실패
        if(scheduleEntity.getRepeatId() != null) {
            throw new CustomException("This value already exists.");
        }

        //반복 생성
        ScheduleRepeatEntity repeatEntity = new ScheduleRepeatEntity(repeatDto);
        ScheduleRepeatEntity createRepeatEntity = scheduleRepeatRepository.save(repeatEntity);

        //기준 일정의 데이터 반복 일정 적용
        scheduleEntity.setRepeatId(createRepeatEntity.getId());
        scheduleRepository.save(scheduleEntity);

        //반복 일정 생성 (알림 포함)
        scheduleBatchRepository.saveRepeatAll(scheduleEntity, createRepeatEntity);

        return new ScheduleRepeatDto(createRepeatEntity);
    }

    public boolean isModified(int repeatId, ScheduleRepeatDto scheduleRepeatDto) {
        ScheduleRepeatEntity repeatEntity = scheduleRepeatRepository.findById(repeatId).orElseThrow(
                () -> new EntityNotFoundException("ScheduleRepeatEntity not found with id: " + repeatId)
        );

        return !scheduleRepeatDto.equals(new ScheduleRepeatDto(repeatEntity));
    }

    public Optional<ScheduleRepeatDto> findById(int id) {
        Optional<ScheduleRepeatEntity> findEntity = scheduleRepeatRepository.findById(id);

        return findEntity.map(ScheduleRepeatDto::new);
    }

    public boolean existsById(int id) {
        return scheduleRepeatRepository.existsById(id);
    }




}


