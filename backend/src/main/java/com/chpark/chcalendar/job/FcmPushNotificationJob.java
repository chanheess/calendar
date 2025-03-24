package com.chpark.chcalendar.job;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FcmPushNotificationJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(FcmPushNotificationJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String token = context.getMergedJobDataMap().getString("fcmToken");
        String title = context.getMergedJobDataMap().getString("title");
        String body = context.getMergedJobDataMap().getString("body");
        String url = context.getMergedJobDataMap().getString("url");

        logger.info("Executing FCM Push Notification Job with token: {}", token);
        logger.info("Message details - Title: {}, Body: {}, URL: {}", title, body, url);

        Message message = Message.builder()
                .setToken(token)
                .putData("title", title != null ? title : "")
                .putData("body", body != null ? body : "")
                .putData("url", url != null ? url : "")
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("FCM message sent successfully, response: {}", response);
        } catch (Exception e) {
            logger.error("FCM push notification sending failed: {}", e.getMessage(), e);
            throw new JobExecutionException("FCM 푸시 알림 전송 실패: " + e.getMessage(), e);
        }
    }
}

