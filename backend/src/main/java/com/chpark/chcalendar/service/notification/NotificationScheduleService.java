package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.dto.notification.NotificationDto;
import com.chpark.chcalendar.dto.notification.NotificationScheduleDto;
import com.chpark.chcalendar.entity.GroupUserEntity;
import com.chpark.chcalendar.entity.NotificationEntity;
import com.chpark.chcalendar.enumClass.NotificationCategory;
import com.chpark.chcalendar.enumClass.NotificationType;
import com.chpark.chcalendar.repository.NotificationRepository;
import com.chpark.chcalendar.service.schedule.ScheduleGroupService;
import com.chpark.chcalendar.service.user.GroupUserService;
import com.chpark.chcalendar.service.user.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationScheduleService extends NotificationService {

    public NotificationScheduleService(NotificationRepository notificationRepository, GroupUserService groupUserService, UserService userService, RedisTemplate<String, Object> redisTemplate, ScheduleGroupService scheduleGroupService) {
        super(notificationRepository, groupUserService, userService, redisTemplate);
        messageFrom = "일정에서 ";
    }

    @Transactional
    public void acceptInvite(long userId, NotificationDto notificationDto) {
        //일정에 대한 인원을 추가해줘야함
        deleteNotification(userId, notificationDto);
    }

    @Transactional
    public void sendInviteNotification(long userId, long scheduleId, NotificationScheduleDto notificationSchedule, NotificationCategory category) {
        long groupId = notificationSchedule.getGroupId();

        GroupUserEntity userInfo = groupUserService.checkGroupUserAuthority(userId, groupId);
        notificationSchedule.getScheduleGroupDto().forEach(scheduleGroupDto -> {
            groupUserService.checkGroupUserExists(groupId, scheduleGroupDto.getUserId());

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

}
