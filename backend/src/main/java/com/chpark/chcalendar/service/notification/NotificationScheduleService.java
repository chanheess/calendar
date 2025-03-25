package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.dto.notification.NotificationDto;
import com.chpark.chcalendar.dto.notification.NotificationScheduleDto;
import com.chpark.chcalendar.entity.GroupUserEntity;
import com.chpark.chcalendar.entity.NotificationEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleGroupEntity;
import com.chpark.chcalendar.enumClass.InvitationStatus;
import com.chpark.chcalendar.enumClass.NotificationCategory;
import com.chpark.chcalendar.enumClass.NotificationType;
import com.chpark.chcalendar.exception.ScheduleException;
import com.chpark.chcalendar.repository.NotificationRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleGroupRepository;
import com.chpark.chcalendar.service.user.GroupUserService;
import com.chpark.chcalendar.service.user.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationScheduleService extends NotificationService {

    //순환 참조를 해결하기 위해 어쩔 수 없이 repository 사용
    private final ScheduleGroupRepository scheduleGroupRepository;

    public NotificationScheduleService(NotificationRepository notificationRepository, GroupUserService groupUserService, UserService userService, RedisTemplate<String, Object> redisTemplate, ScheduleGroupRepository scheduleGroupRepository) {
        super(notificationRepository, groupUserService, userService, redisTemplate);
        messageFrom = "일정에서 ";
        this.scheduleGroupRepository = scheduleGroupRepository;
    }

    @Transactional
    @Override
    public void rejectInvite(long userId, NotificationDto notificationDto) {
        updateScheduleGroup(userId, notificationDto.getCategoryId(), InvitationStatus.DECLINED);
        deleteNotification(userId, notificationDto);
    }

    @Transactional
    @Override
    public void pendingInvite(long userId, NotificationDto notificationDto) {
        updateScheduleGroup(userId, notificationDto.getCategoryId(), InvitationStatus.PENDING);
        deleteNotification(userId, notificationDto);
    }

    @Transactional
    @Override
    public void acceptInvite(long userId, NotificationDto notificationDto) {
        updateScheduleGroup(userId, notificationDto.getCategoryId(), InvitationStatus.ACCEPTED);
        deleteNotification(userId, notificationDto);
    }

    @Transactional
    public void sendInviteNotification(long userId, long scheduleId, NotificationScheduleDto notificationSchedule, NotificationCategory category) {
        long groupId = notificationSchedule.getGroupId();

        GroupUserEntity userInfo = groupUserService.getGroupUser(userId, groupId);
        notificationSchedule.getScheduleGroupDto().forEach(scheduleGroupDto -> {

            String message = userInfo.getGroupTitle() + messageFrom + scheduleGroupDto.getUserNickname() + "님을 초대합니다.";
            NotificationEntity entity = new NotificationEntity(
                    scheduleGroupDto.getUserId(),
                    category,
                    scheduleId,
                    NotificationType.INVITE,
                    0L,
                    message,
                    2592000L);

            notificationRepository.save(entity);
        });
    }


    @Transactional
    public void updateScheduleGroup(long userId, long categoryId, InvitationStatus status) {
        ScheduleGroupEntity scheduleGroupEntity = scheduleGroupRepository.findByScheduleIdAndUserId(categoryId, userId).orElse(null);

        if (scheduleGroupEntity == null) {
            return;
        }

        scheduleGroupEntity.setStatus(status);
        scheduleGroupRepository.save(scheduleGroupEntity);
    }

    @Transactional
    public void deleteScheduleNotifications(long scheduleId) {
        String pattern = "user:*:" + NotificationCategory.SCHEDULE + ":" + scheduleId + ":" + NotificationType.INVITE + ":*";
        notificationRepository.deletePatten(pattern);
    }

    @Transactional
    public void deleteScheduleNotification(long userId, long scheduleId) {
        String pattern = "user:" + userId + ":" + NotificationCategory.SCHEDULE + ":" + scheduleId + ":" + NotificationType.INVITE + ":*";
        notificationRepository.deletePatten(pattern);
    }



}
