package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.dto.notification.NotificationDto;
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
    }

    @Transactional
    public void acceptInvite(long userId, NotificationDto notificationDto) {
        //일정에 대한 인원을 추가해줘야함
        deleteNotification(userId, notificationDto);
    }

    //알림 발송 기능 추가해주기
//    public void sendInviteNotification(long userId, groupId, ) {
//        //발송 정보?
//        //메시지 그룹 카테고리 유저아이디 캘린더 아이디
//
//        //스케쥴 그룹을 추가하기 위한 정보가 필요하다?
//        //
//
//    }



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

}
