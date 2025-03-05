package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.entity.FirebaseTokenEntity;
import com.chpark.chcalendar.repository.FirebaseTokenRepository;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.google.firebase.messaging.FirebaseMessagingException;
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
    public void createNotifications(long userId, String jobId, String title, String body, String url, LocalDateTime notificationTime) {
        List<FirebaseTokenEntity> fcmTokenList = firebaseTokenRepository.findByUserId(userId);

        if(fcmTokenList.isEmpty()) {
            return;
        }

        fcmTokenList.forEach(fcmToken -> {
            try {
                quartzSchedulerService.createFcmPushNotification(
                        jobId + fcmToken.getToken(),
                        fcmToken.getToken(),
                        title,
                        body,
                        url,
                        notificationTime
                );
            }
            catch (SchedulerException e) {
                checkTokenExpired(e, userId, fcmToken.getToken());
            }
        });
    }

    @Transactional
    public void updateNotifications(long userId, String jobId, LocalDateTime notificationTime) {
        List<FirebaseTokenEntity> fcmTokenList = firebaseTokenRepository.findByUserId(userId);

        if(fcmTokenList.isEmpty()) {
            return;
        }

        fcmTokenList.forEach(fcmToken -> {
            try {
                quartzSchedulerService.updateFcmPushNotification(jobId + fcmToken.getToken(), notificationTime);
            } catch (SchedulerException e) {
                checkTokenExpired(e, userId, fcmToken.getToken());
            }
        });
    }

    @Transactional
    public void deleteNotifications(long userId, String jobId) {
        List<FirebaseTokenEntity> fcmTokenList = firebaseTokenRepository.findByUserId(userId);

        if(fcmTokenList.isEmpty()) {
            return;
        }

        fcmTokenList.forEach(fcmToken -> {
            try {
                quartzSchedulerService.deleteFcmPushNotification(jobId + fcmToken.getToken());
            } catch (SchedulerException e) {
                checkTokenExpired(e, userId, fcmToken.getToken());
            }
        });
    }

    public void checkTokenExpired(SchedulerException e, long userId, String token) {
        Throwable cause = e.getCause();
        if (cause instanceof FirebaseMessagingException) {
            FirebaseMessagingException fme = (FirebaseMessagingException) cause;
            if ("UNREGISTERED".equals(fme.getMessagingErrorCode().name())) {
                deleteToken(userId, token);
                System.out.println("Deleted expired token: " + token);
            }
        } else {
            throw new RuntimeException(e);
        }
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

    @Transactional
    public void deleteToken(String token) {
        firebaseTokenRepository.deleteByToken(token);
    }
}
