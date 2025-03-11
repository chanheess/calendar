package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.dto.FirebaseTokenDto;
import com.chpark.chcalendar.entity.FirebaseTokenEntity;
import com.chpark.chcalendar.repository.FirebaseTokenRepository;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FirebaseService {

    private final FirebaseTokenRepository firebaseTokenRepository;
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
                        jobId,
                        fcmToken.getToken(),
                        getUserPlatformKey(fcmToken),
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

    @Transactional
    public void saveToken(long userId, FirebaseTokenDto firebaseToken) {
        Optional<FirebaseTokenEntity> firebaseTokenEntity = firebaseTokenRepository.findByUserIdAndPlatform(userId, firebaseToken.getPlatformId());

        if (firebaseTokenEntity.isPresent()) {
            try {
                if (firebaseTokenEntity.get().getToken().equals(firebaseToken.getFirebaseToken())) {
                    return;
                }

                firebaseTokenEntity.get().setToken(firebaseToken.getFirebaseToken());
                firebaseTokenRepository.save(firebaseTokenEntity.get());

                quartzSchedulerService.changeTokenInPushNotification(
                        getUserPlatformKey(firebaseTokenEntity.get()), firebaseTokenEntity.get().getToken());
            } catch (SchedulerException ignored) {

            }

        } else {
            FirebaseTokenEntity tokenEntity = new FirebaseTokenEntity(
                    userId, firebaseToken.getFirebaseToken(), firebaseToken.getPlatformId());
            firebaseTokenRepository.save(tokenEntity);
        }
    }

    @Transactional
    public void deleteToken(long userId, String token) {
        firebaseTokenRepository.deleteByUserIdAndToken(userId, token);
    }

    @Transactional
    public void deleteToken(String token) {
        firebaseTokenRepository.deleteByToken(token);
    }

    public String getUserPlatformKey(FirebaseTokenEntity firebaseToken) {
        return firebaseToken.getUserId() + "-" + firebaseToken.getPlatform();
    }
}
