package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.entity.FirebaseTokenEntity;
import com.chpark.chcalendar.repository.FirebaseTokenRepository;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FirebaseService {

    private final FirebaseTokenRepository firebaseTokenRepository;
    private final UserRepository userRepository;
    private final QuartzSchedulerService quartzSchedulerService;

    @Transactional
    public void sendPushNotification(long userId, String jobId, String title, String body, String url, LocalDateTime notificationTime) {
        List<FirebaseTokenEntity> fcmTokenList = firebaseTokenRepository.findByUserId(userId);

        if(fcmTokenList.isEmpty()) {
            return;
        }

        fcmTokenList.forEach(fcmToken -> {
            try {
                quartzSchedulerService.scheduleFcmPushNotification(
                        jobId,
                        fcmToken.getToken(),
                        title,
                        body,
                        url,
                        notificationTime
                );
            } catch (SchedulerException e) {
                // 실패시 처리 필요
                throw new RuntimeException(e);
            }
        });
    }

    public void saveToken(long userId, String token) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found.");
        }

        FirebaseTokenEntity tokenEntity = new FirebaseTokenEntity(userId, token);

        firebaseTokenRepository.save(tokenEntity);
    }

    @Transactional
    public void deleteToken(long userId, String token) {
        firebaseTokenRepository.deleteByUserIdAndToken(userId, token);
    }
}
