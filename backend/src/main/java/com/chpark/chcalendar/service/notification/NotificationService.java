package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.dto.notification.NotificationDto;
import com.chpark.chcalendar.dto.notification.NotificationScheduleDto;
import com.chpark.chcalendar.entity.NotificationEntity;
import com.chpark.chcalendar.entity.calendar.CalendarMemberEntity;
import com.chpark.chcalendar.enumClass.CalendarMemberRole;
import com.chpark.chcalendar.enumClass.NotificationCategory;
import com.chpark.chcalendar.enumClass.NotificationType;
import com.chpark.chcalendar.repository.NotificationRepository;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.service.calendar.CalendarMemberService;
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

    protected final NotificationRepository notificationRepository;

    protected final CalendarMemberService calendarMemberService;
    protected final UserRepository userRepository;

    protected final RedisTemplate<String, Object> redisTemplate;

    protected String messageFrom;


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

    public void sendInviteNotification(long userId, long calendarId, NotificationCategory category, String nickname) {
        Long inviteUserId = userRepository.findIdByNickname(nickname).orElseThrow(
                () -> new EntityNotFoundException("사용자를 찾을 수 없습니다.")
        );

        CalendarMemberEntity calendarMember = calendarMemberService.checkCalendarMemberAuthority(userId, calendarId, CalendarMemberRole.USER);
        calendarMemberService.checkCalendarMemberExists(calendarId, inviteUserId);
        //TODO: 캘린더 최대 가입자 수 추가

        String message = calendarMember.getCalendar().getTitle() + messageFrom + nickname + "님을 초대합니다.";
        NotificationEntity entity = new NotificationEntity(
                inviteUserId,
                category,
                calendarId,
                NotificationType.INVITE,
                0L,
                message,
                2592000L);

        notificationRepository.save(entity);
    }

    @Transactional
    public void sendInviteNotification(long userId, long scheduleId, NotificationScheduleDto notificationScheduleList, NotificationCategory category) {
        //자식 클래스에서 정의
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
    public void rejectNotificationByCategory(long userId, NotificationDto notificationDto) {
        switch (notificationDto.getType()) {
            case INFO -> {
            }
            case INVITE -> {
                rejectInvite(userId, notificationDto);
            }
        }
    }

    @Transactional
    public void pendingNotificationByCategory(long userId, NotificationDto notificationDto) {
        switch (notificationDto.getType()) {
            case INFO -> {
            }
            case INVITE -> {
                pendingInvite(userId, notificationDto);
            }
        }
    }

    @Transactional
    public void acceptInvite(long userId, NotificationDto notificationDto) {
        //자식 클래스에서 정의
    }

    @Transactional
    public void rejectInvite(long userId, NotificationDto notificationDto) {
        deleteNotification(userId, notificationDto);
    }

    @Transactional
    public void pendingInvite(long userId, NotificationDto notificationDto) {
        //자식 클래스에서 정의
    }

}
