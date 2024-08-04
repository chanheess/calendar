package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleRepeatDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.entity.ScheduleRepeatEntity;
import com.chpark.calendar.exception.CustomException;
import com.chpark.calendar.repository.ScheduleBatchRepository;
import com.chpark.calendar.repository.ScheduleNotificationRepository;
import com.chpark.calendar.repository.ScheduleRepeatRepository;
import com.chpark.calendar.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.sql.SQLException;

@RequiredArgsConstructor
@Slf4j
@Service
public class ScheduleRepeatService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleRepeatRepository scheduleRepeatRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final ScheduleBatchRepository scheduleBatchRepository;

    @Transactional
    public ScheduleRepeatDto.Response create(int scheduleId, ScheduleRepeatDto repeatDto) throws SQLException{

        if(scheduleRepeatRepository.existsById(scheduleId)) {
            throw new CustomException("This value already exists.");
        }

        //기준 일정 가져오기
        Optional<ScheduleEntity> scheduleEntity = scheduleRepository.findById(scheduleId);
        if(scheduleEntity.isEmpty()) {
            throw new EntityNotFoundException("Schedule not found with id: " + scheduleId);
        }

        //반복 생성
        ScheduleRepeatEntity repeatEntity = new ScheduleRepeatEntity(repeatDto);
        ScheduleRepeatEntity createRepeatEntity = scheduleRepeatRepository.save(repeatEntity);

        //기준 일정의 데이터 반복 일정 적용
        scheduleEntity.get().setRepeatId(createRepeatEntity.getId());
        scheduleRepository.save(scheduleEntity.get());

        //일정 생성후 일정 알림 생성
        scheduleBatchRepository.saveRepeatAll(scheduleEntity.get(), createRepeatEntity);

        return new ScheduleRepeatDto.Response(createRepeatEntity);
    }

    public ScheduleRepeatDto.Response findById(int id) {

        Optional<ScheduleRepeatEntity> findEntity = scheduleRepeatRepository.findById(id);

        if(findEntity.isPresent()) {
            return new ScheduleRepeatDto.Response(findEntity.get());
        } else {
            throw new EntityNotFoundException("ScheduleRepeat not found with id: " + id);
        }
    }

    public boolean existsById(int id) {
        return scheduleRepeatRepository.existsById(id);
    }


    public ScheduleRepeatDto.Response currentScheduleUpdate(int id, ScheduleRepeatDto scheduleRepeatDto) {

        //현재 일정만 바꾼다.

        return new ScheduleRepeatDto.Response();
    }

    public ScheduleRepeatDto.Response allScheduleUpdate(int id, ScheduleRepeatDto scheduleRepeatDto) {
        //모든 일정을 바꿔준다.

        //기준 일정에서부터 이전 일정이 있는가?

        //없다면 현재 repeat id에 해당하는 것 수정

        //있다면 새로 생성해서 일정들 수정 일정이 추가될 수도?

        return new ScheduleRepeatDto.Response();
    }

}


