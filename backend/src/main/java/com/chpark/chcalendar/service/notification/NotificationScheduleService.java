package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.dto.notification.NotificationDto;
import com.chpark.chcalendar.dto.notification.NotificationScheduleDto;
import com.chpark.chcalendar.entity.NotificationEntity;
import com.chpark.chcalendar.entity.calendar.CalendarMemberEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleGroupEntity;
import com.chpark.chcalendar.enumClass.InvitationStatus;
import com.chpark.chcalendar.enumClass.NotificationCategory;
import com.chpark.chcalendar.enumClass.NotificationType;
import com.chpark.chcalendar.repository.NotificationRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleGroupRepository;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.service.calendar.CalendarMemberService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class NotificationScheduleService extends NotificationService {

    private final ScheduleGroupRepository scheduleGroupRepository;

    public NotificationScheduleService(NotificationRepository notificationRepository, CalendarMemberService calendarMemberService, UserRepository userRepository, RedisTemplate<String, Object> redisTemplate, ScheduleGroupRepository scheduleGroupRepository) {
        super(notificationRepository, calendarMemberService, userRepository, redisTemplate);
        this.scheduleGroupRepository = scheduleGroupRepository;
        messageFrom = "일정에서 ";
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
        long calendarId = notificationSchedule.getCalendarId();

        CalendarMemberEntity calendarMember = calendarMemberService.getCalendarMember(userId, calendarId);
        notificationSchedule.getScheduleGroupDto().forEach(scheduleGroupDto -> {
            String message = calendarMember.getCalendar().getTitle() + messageFrom + scheduleGroupDto.getUserNickname() + "님을 초대합니다.";
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
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Transactional
    public void deleteScheduleNotification(long userId, long scheduleId) {
        String pattern = "user:" + userId + ":" + NotificationCategory.SCHEDULE + ":" + scheduleId + ":" + NotificationType.INVITE + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }



}
