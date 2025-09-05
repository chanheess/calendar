package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.calendar.CalendarMemberDto;
import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleGroupEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleNotificationEntity;
import com.chpark.chcalendar.exception.authorization.GroupAuthorizationException;
import com.chpark.chcalendar.repository.schedule.ScheduleGroupRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleNotificationRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.service.calendar.CalendarMemberService;
import com.chpark.chcalendar.service.notification.FirebaseService;
import com.chpark.chcalendar.utility.ScheduleUtility;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScheduleNotificationService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final ScheduleGroupRepository scheduleGroupRepository;

    private final FirebaseService firebaseService;
    private final CalendarMemberService calendarMemberService;

    @Value("${home_url}")
    String homeUrl;


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
            createNotificationScheduler(userId, scheduleEntity, notification);
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
    public List<ScheduleNotificationDto> update(long userId, long scheduleId, List<ScheduleNotificationDto> notifications) {
        List<ScheduleNotificationEntity> resultEntities = scheduleNotificationRepository.findByScheduleId(scheduleId);
        List<ScheduleNotificationDto> updatedNotifications = new ArrayList<>();

        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with id: " + scheduleId)
        );

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
                updateNotificationScheduler(userId, scheduleEntity, resultEntity);
            } else if (i < newSize) {
                // 새로 생성해야 할 DTO가 있는 경우
                ScheduleNotificationEntity newEntity = new ScheduleNotificationEntity(scheduleId, notifications.get(i));
                newEntity = scheduleNotificationRepository.save(newEntity);

                updatedNotifications.add(new ScheduleNotificationDto(newEntity));
                createNotificationScheduler(userId, scheduleEntity, newEntity);
            } else {
                // 삭제해야 할 기존 엔티티가 있는 경우
                ScheduleNotificationEntity resultEntity = resultEntities.get(i);
                deleteScheduleNotification(userId, scheduleEntity, resultEntity);
            }
        }

        return updatedNotifications;
    }

    @Transactional
    public void deleteNotificationList(long userId, long scheduleId) {
        Optional<ScheduleEntity> scheduleEntity = scheduleRepository.findByIdAndUserId(scheduleId, userId);
        if (scheduleEntity.isEmpty()) return;

        deleteNotificationList(scheduleEntity.get());
    }

    @Transactional
    public void deleteNotificationList(ScheduleEntity scheduleEntity) {
        if (scheduleEntity == null) {
            return;
        }

        List<ScheduleNotificationEntity> notificationList = scheduleNotificationRepository.findByScheduleId(scheduleEntity.getId());

        notificationList.forEach(notification -> {
                    deleteScheduleNotification(scheduleEntity.getUserId(), scheduleEntity, notification);
                }
        );
    }

    @Transactional
    public void deleteScheduleNotification(long userId, ScheduleEntity scheduleEntity, ScheduleNotificationEntity notificationEntity) {
        deleteNotificationScheduler(userId, scheduleEntity, notificationEntity);
        scheduleNotificationRepository.delete(notificationEntity);
    }

    @Transactional
    public void createNotificationScheduler(long userId, ScheduleEntity scheduleEntity, ScheduleNotificationEntity notification) {
        String body = ScheduleUtility.formatNotificationDate(scheduleEntity.getStartAt(), notification.getNotificationAt());

        //멤버 가져오기
        List<Long> targetUserIds = getUserIdList(userId, scheduleEntity);

        // 대상 사용자에게 알림 전송
        targetUserIds.forEach(targetUserId -> {
                String jobId = getJobId(targetUserId, scheduleEntity.getId(), notification.getId());
                firebaseService.createNotifications(
                        targetUserId,
                        jobId,
                        scheduleEntity.getTitle(),
                        body,
                        homeUrl,
                        notification.getNotificationAt()
                );
            }
        );
    }

    @Transactional
    public void updateNotificationScheduler(long userId, ScheduleEntity scheduleEntity, ScheduleNotificationEntity notification) {
        String body = ScheduleUtility.formatNotificationDate(scheduleEntity.getStartAt(), notification.getNotificationAt());

        List<Long> targetUserIds = getUserIdList(userId, scheduleEntity);

        targetUserIds.forEach(targetUserId -> {
                    String jobId = getJobId(targetUserId, scheduleEntity.getId(), notification.getId());
                    firebaseService.updateNotifications(
                            targetUserId,
                            jobId,
                            scheduleEntity.getTitle(),
                            body,
                            homeUrl,
                            notification.getNotificationAt()
                    );
            }
        );
    }

    @Transactional
    public void deleteNotificationScheduler(long userId, ScheduleEntity scheduleEntity, ScheduleNotificationEntity notification) {
        List<Long> targetUserIds = getUserIdList(userId, scheduleEntity);

        targetUserIds.forEach(targetUserId -> {
                    String jobId = getJobId(targetUserId, scheduleEntity.getId(), notification.getId());
                    firebaseService.deleteNotifications(targetUserId, jobId);
                }
        );
    }

    private String getJobId(long userId, long scheduleId, long notificationId) {
        return "user" + userId + "-schedule" +  scheduleId + "-notification" + notificationId;
    }

    @Transactional
    public List<Long> getUserIdList(long userId, ScheduleEntity scheduleEntity) {
        // 일정 그룹이 있으면 일정 그룹에 대해서 반환
        List<ScheduleGroupEntity> groupList = scheduleGroupRepository.findByScheduleId(scheduleEntity.getId());
        if (!groupList.isEmpty()) {
            return groupList.stream()
                    .map(ScheduleGroupEntity::getUserId)
                    .collect(Collectors.toList());
        }

        // 일정 그룹이 없으면 캘린더 멤버 조회
        try {
            return calendarMemberService.findCalendarMemberList(userId, scheduleEntity.getCalendarId())
                    .stream()
                    .map(CalendarMemberDto::getUserId)
                    .collect(Collectors.toList());
        } catch (GroupAuthorizationException e) {
            // 그룹 캘린더가 아니면 일정 소유자만 반환
            return List.of(userId);
        }
    }
}
