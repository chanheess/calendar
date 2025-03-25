package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleRepeatDto;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleRepeatEntity;
import com.chpark.chcalendar.exception.CustomException;
import com.chpark.chcalendar.repository.schedule.ScheduleBatchRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepeatRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
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

    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public ScheduleRepeatDto create(long scheduleId, ScheduleRepeatDto repeatDto, long userId) {
        if(repeatDto == null) {
            return new ScheduleRepeatDto();
        }

        //기준 일정 가져오기
        ScheduleEntity scheduleEntity = scheduleRepository.findByIdAndUserId(scheduleId, userId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with id: " + scheduleId)
        );

        //이미 반복이 만들어져있다면 실패
        if(scheduleEntity.getRepeatId() != null) {
            throw new CustomException("This value already exists.");
        }

        //반복 생성
        ScheduleRepeatEntity repeatEntity = new ScheduleRepeatEntity(repeatDto, scheduleEntity.getId());
        ScheduleRepeatEntity createRepeatEntity = scheduleRepeatRepository.save(repeatEntity);

        //기준 일정의 데이터 반복 일정 적용
        scheduleEntity.setRepeatId(createRepeatEntity.getId());
        scheduleRepository.save(scheduleEntity);

        //반복 일정 생성 (알림 포함)
        scheduleBatchRepository.saveRepeatAll(scheduleEntity, createRepeatEntity);

        return new ScheduleRepeatDto(createRepeatEntity);
    }

    public boolean isModified(long repeatId, ScheduleRepeatDto scheduleRepeatDto) {
        ScheduleRepeatEntity repeatEntity = scheduleRepeatRepository.findById(repeatId).orElseThrow(
                () -> new EntityNotFoundException("ScheduleRepeatEntity not found with id: " + repeatId)
        );

        return !scheduleRepeatDto.equals(new ScheduleRepeatDto(repeatEntity));
    }

    public Optional<ScheduleRepeatDto> findById(long id) {
        Optional<ScheduleRepeatEntity> findEntity = scheduleRepeatRepository.findById(id);

        return findEntity.map(ScheduleRepeatDto::new);
    }

    public boolean existsById(long id) {
        return scheduleRepeatRepository.existsById(id);
    }


    public boolean isMasterSchedule(Long repeatId, long scheduleId) {
        if (repeatId == null) {
            return false;
        }

        ScheduleRepeatEntity repeatEntity = scheduleRepeatRepository.findById(repeatId).orElseThrow(
                () -> new EntityNotFoundException("Repeat not found")
        );

        return repeatEntity.getMasterScheduleId() == scheduleId;
    }

}


