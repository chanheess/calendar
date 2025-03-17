package com.chpark.chcalendar.service.notification;


import com.chpark.chcalendar.dto.notification.NotificationDto;
import com.chpark.chcalendar.repository.NotificationRepository;
import com.chpark.chcalendar.service.user.GroupUserService;
import com.chpark.chcalendar.service.user.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationGroupService extends NotificationService {

    public NotificationGroupService(NotificationRepository notificationRepository, GroupUserService groupUserService, UserService userService, RedisTemplate<String, Object> redisTemplate) {
        super(notificationRepository, groupUserService, userService, redisTemplate);
    }

    @Transactional
    public void acceptInvite(long userId, NotificationDto notificationDto) {
        String nickname = userService.findNickname(userId);
        groupUserService.addUser(userId, nickname, notificationDto.getCategoryId());
        deleteNotification(userId, notificationDto);
    }

}
