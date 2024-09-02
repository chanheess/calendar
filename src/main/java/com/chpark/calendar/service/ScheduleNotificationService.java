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
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScheduleNotificationService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;


    @Transactional
    public List<ScheduleNotificationDto> create(int scheduleId, List<ScheduleNotificationDto> notifications) {
        if(notifications.isEmpty()) {
            return new ArrayList<>();
        }

        if (scheduleRepository.existsById(scheduleId)) {
            // DTO 리스트를 엔티티 리스트로 변환
            List<ScheduleNotificationEntity> notificationEntities = ScheduleNotificationEntity.fromScheduleNotificationDtoList(scheduleId, notifications);

            // 엔티티 리스트를 저장
            List<ScheduleNotificationEntity> savedEntities = scheduleNotificationRepository.saveAll(notificationEntities);

            // 저장된 엔티티 리스트를 DTO 리스트로 변환하여 반환
            return ScheduleNotificationDto.fromScheduleNotificationEntityList(savedEntities);
        } else {
            // scheduleId가 존재하지 않는 경우 빈 리스트 반환
            throw new EntityNotFoundException("Schedule not found with id: " + scheduleId);
        }
    }

    public List<ScheduleNotificationDto> findByScheduleId(int id) {
        return ScheduleNotificationDto.fromScheduleNotificationEntityList(scheduleNotificationRepository.findByScheduleId(id));
    }

    public boolean existsByScheduleId(int scheduleId) {
        return scheduleNotificationRepository.existsByScheduleId(scheduleId);
    }

    @Transactional
    public List<ScheduleNotificationDto> update(int scheduleId, List<ScheduleNotificationDto> notifications) {

        List<ScheduleNotificationEntity> resultEntities = scheduleNotificationRepository.findByScheduleId(scheduleId);
        List<ScheduleNotificationDto> updatedNotifications = new ArrayList<>();

        int existingSize = resultEntities.size();
        int newSize = notifications.size();

        // 매칭 및 업데이트, 새로 생성, 남은 엔티티 삭제
        for (int i = 0; i < Math.max(existingSize, newSize); i++) {
            if (i < newSize && i < existingSize) {
                // 매칭된 엔티티를 업데이트
                ScheduleNotificationEntity resultEntity = resultEntities.get(i);
                resultEntity.setNotificationAt(notifications.get(i).getNotificationAt());
                resultEntity = scheduleNotificationRepository.save(resultEntity);
                updatedNotifications.add(new ScheduleNotificationDto(resultEntity));
            } else if (i < newSize) {
                // 새로 생성해야 할 DTO가 있는 경우
                ScheduleNotificationEntity newEntity = new ScheduleNotificationEntity(scheduleId, notifications.get(i));
                newEntity = scheduleNotificationRepository.save(newEntity);
                updatedNotifications.add(new ScheduleNotificationDto(newEntity));
            } else {
                // 삭제해야 할 기존 엔티티가 있는 경우
                scheduleNotificationRepository.delete(resultEntities.get(i));
            }
        }

        return updatedNotifications;
    }

    @Transactional
    public void deleteByScheduleId(int scheduleId) {
        scheduleNotificationRepository.deleteByScheduleId(scheduleId);
    }

}
