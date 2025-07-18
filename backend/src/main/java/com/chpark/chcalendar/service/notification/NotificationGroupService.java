package com.chpark.chcalendar.service.notification;


import com.chpark.chcalendar.dto.notification.NotificationDto;
import com.chpark.chcalendar.repository.NotificationRepository;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.service.calendar.CalendarMemberService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationGroupService extends NotificationService {

    public NotificationGroupService(NotificationRepository notificationRepository, CalendarMemberService calendarMemberService, UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        super(notificationRepository, calendarMemberService, userRepository, redisTemplate);
        messageFrom = "캘린더에서 ";
    }

    @Transactional
    @Override
    public void acceptInvite(long userId, NotificationDto notificationDto) {
        calendarMemberService.addUser(userId, notificationDto.getCategoryId());
        deleteNotification(userId, notificationDto);
    }

    @Transactional
    @Override
    public void rejectInvite(long userId, NotificationDto notificationDto) {
        super.rejectInvite(userId, notificationDto);
    }

}
