package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleRepeatDto;
import com.chpark.chcalendar.entity.schedule.*;
import com.chpark.chcalendar.exception.CustomException;
import com.chpark.chcalendar.repository.schedule.ScheduleBatchRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleGroupRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepeatRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ScheduleRepeatService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleRepeatRepository scheduleRepeatRepository;
    private final ScheduleBatchRepository scheduleBatchRepository;
    private final ScheduleNotificationService scheduleNotificationService;

    @Transactional
    public ScheduleRepeatDto create(long scheduleId, ScheduleRepeatDto repeatDto, long userId) {
        if (repeatDto == null) {
            return new ScheduleRepeatDto();
        }

        //기준 일정 가져오기
        ScheduleEntity scheduleEntity = scheduleRepository.findByIdAndUserId(scheduleId, userId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with id: " + scheduleId)
        );

        //이미 반복이 만들어져있다면 실패
        if (scheduleEntity.getRepeatId() != null) {
            throw new CustomException("This value already exists.");
        }

        //반복 생성
        ScheduleRepeatEntity repeatEntity = new ScheduleRepeatEntity(repeatDto);
        ScheduleRepeatEntity createRepeatEntity = scheduleRepeatRepository.save(repeatEntity);

        //기준 일정의 데이터 반복 일정 적용
        scheduleEntity.setRepeatId(createRepeatEntity.getId());
        scheduleRepository.save(scheduleEntity);

        //반복 일정 생성
        ScheduleBatchEntity scheduleBatchEntity = scheduleBatchRepository.saveRepeatAll(scheduleEntity, createRepeatEntity);
        createRepeatScheduler(userId, scheduleBatchEntity);

        return new ScheduleRepeatDto(createRepeatEntity);
    }

    public Optional<ScheduleRepeatDto> findById(long id) {
        Optional<ScheduleRepeatEntity> findEntity = scheduleRepeatRepository.findById(id);

        return findEntity.map(ScheduleRepeatDto::new);
    }

    @Transactional
    public void deleteRepeat(long id) {
        scheduleRepeatRepository.deleteById(id);
    }

    @Transactional
    public void createRepeatScheduler(long userId, ScheduleBatchEntity scheduleBatchEntity) {
        Map<Long, ScheduleEntity> scheduleList = scheduleBatchEntity.getSchedules();
        List<ScheduleNotificationEntity> notificationList = scheduleBatchEntity.getNotifications();

        // 스케쥴러 생성
        for (ScheduleNotificationEntity notification : notificationList) {
            ScheduleEntity schedule = scheduleList.get(notification.getScheduleId());
            scheduleNotificationService.createNotificationScheduler(userId, schedule, notification);
        }
    }

}


