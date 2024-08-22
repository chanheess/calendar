package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleNotificationDto;
import com.chpark.calendar.entity.ScheduleNotificationEntity;
import com.chpark.calendar.repository.ScheduleNotificationRepository;
import com.chpark.calendar.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScheduleNotificationService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;


    @Transactional
    public ScheduleNotificationDto.Response create(int scheduleId, ScheduleNotificationDto notification) {

        if (scheduleRepository.existsById(scheduleId)) {
            ScheduleNotificationEntity notificationEntity = new ScheduleNotificationEntity(scheduleId, notification);
            ScheduleNotificationEntity savedEntity = scheduleNotificationRepository.save(notificationEntity);

            return new ScheduleNotificationDto.Response(savedEntity);
        } else {
            // scheduleId가 존재하지 않는 경우 빈 리스트 반환
            throw new EntityNotFoundException("Schedule not found with id: " + scheduleId);
        }
    }

    @Transactional
    public List<ScheduleNotificationDto.Response> create(int scheduleId, List<ScheduleNotificationDto> notifications) {

        if (scheduleRepository.existsById(scheduleId)) {
            List<ScheduleNotificationDto.Response> resultNotificationList = new ArrayList<>();

            for (ScheduleNotificationDto notification : notifications) {
                ScheduleNotificationEntity notificationEntity = new ScheduleNotificationEntity(scheduleId, notification);
                ScheduleNotificationEntity savedEntity = scheduleNotificationRepository.save(notificationEntity);
                ScheduleNotificationDto.Response resultNotification = new ScheduleNotificationDto.Response(savedEntity);
                resultNotificationList.add(resultNotification);
            }

            return resultNotificationList;
        } else {
            // scheduleId가 존재하지 않는 경우 빈 리스트 반환
            return Collections.emptyList();
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

    public ScheduleNotificationDto.Response update(int notificationId, ScheduleNotificationDto notificationDto) {

        ScheduleNotificationEntity resultEntity = scheduleNotificationRepository.findById(notificationId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with id: " + notificationId)
        );
        resultEntity.setNotificationAt(notificationDto.getNotificationAt());

        return new ScheduleNotificationDto.Response(scheduleNotificationRepository.save(resultEntity));
    }

    public void deleteById(int notificationId) {
        scheduleNotificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteByScheduleId(int scheduleId) {
        scheduleNotificationRepository.deleteByScheduleId(scheduleId);
    }


}
