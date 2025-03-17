package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.dto.notification.NotificationDto;
import com.chpark.chcalendar.entity.GroupUserEntity;
import com.chpark.chcalendar.entity.NotificationEntity;
import com.chpark.chcalendar.enumClass.NotificationCategory;
import com.chpark.chcalendar.enumClass.NotificationType;
import com.chpark.chcalendar.repository.NotificationRepository;
import com.chpark.chcalendar.service.user.GroupUserService;
import com.chpark.chcalendar.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class NotificationService {

    protected final NotificationRepository notificationRepository;

    protected final GroupUserService groupUserService;
    protected final UserService userService;

    protected final RedisTemplate<String, Object> redisTemplate;


    //알림 가져오기
    @Transactional
    public List<NotificationDto> getNotifications(long userId) {

        Set<String> keys = notificationRepository.scanKeys("user:" + userId + "*");
        List<NotificationDto> result = new ArrayList<>();

        for (String key : keys) {
            NotificationEntity value = (NotificationEntity) redisTemplate.opsForValue().get(key);

            // 값이 null인 경우 처리하지 않음
            if (value == null) {
                continue;
            }

            result.add(new NotificationDto(value));
        }

        return result;
    }

    public void sendInviteNotification(long userId, long groupId, String nickname) {

        long inviteUserId = userService.findUserId(nickname);

        GroupUserEntity userInfo = groupUserService.checkGroupUserAuthority(userId, groupId);
        groupUserService.checkGroupUserExists(groupId, inviteUserId);

        String message = userInfo.getGroupTitle() + "캘린더에서 " + nickname + "님을 초대합니다.";
        NotificationEntity entity = new NotificationEntity(
                inviteUserId,
                NotificationCategory.GROUP,
                groupId,
                NotificationType.INVITE,
                0L,
                message,
                2592000L);

        notificationRepository.save(entity);
    }

    public void deleteNotification(long userId, NotificationDto notificationDto) {
        NotificationEntity notification = new NotificationEntity(userId, notificationDto, 0L);
        notificationRepository.delete(notification.getKey());
    }

    @Transactional
    public void acceptNotificationByCategory(long userId, NotificationDto notificationDto) {
        switch (notificationDto.getType()) {
            case INFO -> {
            }
            case INVITE -> {
                acceptInvite(userId, notificationDto);
            }
        }
    }

    @Transactional
    public void acceptInvite(long userId, NotificationDto notificationDto) {
        //자식 클래스에서 정의
    }

}
