package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleNotificationDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.entity.ScheduleNotificationEntity;
import com.chpark.calendar.exception.CustomException;
import com.chpark.calendar.repository.ScheduleNotificationRepository;
import com.chpark.calendar.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScheduleNotificationService {


    private final ScheduleRepository scheduleRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;

    @Transactional
    public Optional<ScheduleNotificationDto.Response> create(int scheduleId, ScheduleNotificationDto.Request scheduleNotificationDto) {

        if(scheduleRepository.existsById(scheduleId)) {
            ScheduleNotificationEntity notificationEntity = new ScheduleNotificationEntity(scheduleId, scheduleNotificationDto);
            ScheduleNotificationDto.Response resultDto = new ScheduleNotificationDto.Response(scheduleNotificationRepository.save(notificationEntity));

            return Optional.of(resultDto);
        } else {
            return Optional.empty();
        }
    }

    public List<ScheduleNotificationDto.Response> findByScheduleId(int id) {
        return ScheduleNotificationDto.Response.fromScheduleNotificationEntityList(scheduleNotificationRepository.findByScheduleId(id));
    }

    public Optional<ScheduleNotificationDto.Response> findById(int id) {
        Optional<ScheduleNotificationEntity> findEntity = scheduleNotificationRepository.findById(id);

        if(findEntity.isPresent()) {
            ScheduleNotificationDto.Response resultResponse = new ScheduleNotificationDto.Response(findEntity.get());

            return Optional.of(resultResponse);
        } else {
            return Optional.empty();
        }
    }

    public boolean existsById(int id) {
        return scheduleNotificationRepository.existsById(id);
    }

    public boolean existsByScheduleId(int scheduleId) {
        return scheduleNotificationRepository.existsByScheduleId(scheduleId);
    }

    public Optional<ScheduleNotificationDto.Response> update(int notificationId, ScheduleNotificationDto.Request notificationDto) {

        Optional<ScheduleNotificationEntity> updateData = scheduleNotificationRepository.findById(notificationId);

        if(updateData.isPresent()) {
            ScheduleNotificationEntity resultEntity = updateData.get();
            resultEntity.setNotificationAt(notificationDto.getNotificationAt());

            ScheduleNotificationDto.Response resultDto = new ScheduleNotificationDto.Response(scheduleNotificationRepository.save(resultEntity));

            return Optional.of(resultDto);
        } else {
            return Optional.empty();
        }
    }

    public void deleteById(int notificationId) {
        scheduleNotificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteByScheduleId(int scheduleId) {
        scheduleNotificationRepository.deleteByScheduleId(scheduleId);
    }


}
