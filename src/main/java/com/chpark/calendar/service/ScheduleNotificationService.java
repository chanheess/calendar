package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleNotificationDto;
import com.chpark.calendar.entity.ScheduleNotificationEntity;
import com.chpark.calendar.repository.ScheduleNotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScheduleNotificationService {

    private final ScheduleNotificationRepository scheduleNotificationRepository;

    public ScheduleNotificationService(ScheduleNotificationRepository repository) {
        this.scheduleNotificationRepository = repository;
    }

    public Optional<ScheduleNotificationDto.Response> create(int scheduleId, ScheduleNotificationDto.Request scheduleNotificationDto) {
        ScheduleNotificationEntity savedEntity = scheduleNotificationRepository.save(new ScheduleNotificationEntity(scheduleId, scheduleNotificationDto));

        return Optional.of(new ScheduleNotificationDto.Response(savedEntity));
    }

    public List<ScheduleNotificationDto.Response> findByScheduleId(int id) {
        return ScheduleNotificationDto.Response.fromScheduleNotificationEntityList(scheduleNotificationRepository.findByScheduleId(id));
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
    public void deleteNotifications(int scheduleId) {
        scheduleNotificationRepository.deleteByScheduleId(scheduleId);
    }


}
