package com.chpark.calendar.service.notification;

import com.chpark.calendar.dto.NotificationDto;
import com.chpark.calendar.entity.group.GroupUserEntity;
import com.chpark.calendar.entity.NotificationEntity;
import com.chpark.calendar.enumClass.GroupAuthority;
import com.chpark.calendar.enumClass.NotificationCategory;
import com.chpark.calendar.enumClass.NotificationType;
import com.chpark.calendar.exception.GroupAuthorityException;
import com.chpark.calendar.repository.NotificationRepository;
import com.chpark.calendar.repository.group.GroupUserRepository;
import com.chpark.calendar.repository.user.UserRepository;
import com.chpark.calendar.service.group.GroupUserService;
import jakarta.persistence.EntityNotFoundException;
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

    private final UserRepository userRepository;
    private final GroupUserRepository groupUserRepository;
    private final NotificationRepository notificationRepository;

    private final GroupUserService groupUserService;

    private final RedisTemplate<String, Object> redisTemplate;


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

    public void sendGroupInviteNotification(long userId, long groupId, String nickname) {

        long inviteUserId = userRepository.findIdByNickname(nickname).orElseThrow(
                () -> new EntityNotFoundException("User does not exist.")
        );

        GroupUserEntity userInfo = this.checkInvitationAuthority(groupId, userId);
        this.checkGroupUserExists(groupId, inviteUserId);

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

    public GroupUserEntity checkInvitationAuthority(long groupId, long userId) {

        GroupUserEntity userInfo = groupUserRepository.findByGroupIdAndUserId(groupId, userId).orElseThrow(
                () -> new GroupAuthorityException("권한이 없습니다.")
        );

        if (userInfo.getRole().compareTo(GroupAuthority.USER) >= 0) {
            throw new GroupAuthorityException("권한이 없습니다.");
        }

        return userInfo;
    }

    public void checkGroupUserExists(long groupId, long userId) {
        if(groupUserRepository.findByGroupIdAndUserId(groupId, userId).isPresent()) {
            throw new IllegalArgumentException("The user is already registered.");
        }
    }

    public void deleteNotification(long userId, NotificationDto notificationDto) {
        NotificationEntity notification = new NotificationEntity(userId, notificationDto, 0L);
        notificationRepository.delete(notification.getKey());
    }

    @Transactional
    public void acceptNotificationByCategory(long userId, NotificationDto notificationDto) {

        //TODO: 추후 리팩토링 할 것
        switch (notificationDto.getCategory()) {
            case SCHEDULE -> {
            }
            case GROUP -> {
                switch (notificationDto.getType()) {
                    case INFO -> {
                    }
                    case INVITE -> {
                        groupUserService.addUser(userId, notificationDto.getCategoryId());
                        this.deleteNotification(userId, notificationDto);
                    }
                }
            }
        }
    }

}
