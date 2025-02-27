package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleNotificationEntity;
import com.chpark.chcalendar.repository.schedule.ScheduleNotificationRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.service.notification.FirebaseService;
import com.chpark.chcalendar.utility.ScheduleUtility;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScheduleNotificationService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;

    private final FirebaseService firebaseService;


    @Transactional
    public List<ScheduleNotificationDto> create(long userId, long scheduleId, List<ScheduleNotificationDto> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return new ArrayList<>();
        }

        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId).orElseThrow(
            () -> new EntityNotFoundException("Schedule not found with id: " + scheduleId)
        );

        List<ScheduleNotificationEntity> notificationEntities = ScheduleNotificationEntity.fromScheduleNotificationDtoList(scheduleId, notifications);
        notificationEntities = scheduleNotificationRepository.saveAll(notificationEntities);

        notificationEntities.forEach(notification -> {
            String jobId = "schedule-" + userId + "-" + notification.getScheduleId();

            firebaseService.sendPushNotification(
                userId,
                jobId,
                scheduleEntity.getTitle(),
                ScheduleUtility.formatNotificationDate(scheduleEntity.getStartAt(), notification.getNotificationAt()),
                "https://localhost:3000",
                notification.getNotificationAt()
            );
        });

        return ScheduleNotificationDto.fromScheduleNotificationEntityList(notificationEntities);
    }

    public List<ScheduleNotificationDto> findByScheduleId(long id) {
        return ScheduleNotificationDto.fromScheduleNotificationEntityList(scheduleNotificationRepository.findByScheduleId(id));
    }

    public boolean existsByScheduleId(long scheduleId) {
        return scheduleNotificationRepository.existsByScheduleId(scheduleId);
    }

    @Transactional
    public List<ScheduleNotificationDto> update(long scheduleId, List<ScheduleNotificationDto> notifications) {

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
    public void deleteByScheduleId(long scheduleId) {
        scheduleNotificationRepository.deleteByScheduleId(scheduleId);
    }

}
