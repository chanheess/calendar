package com.chpark.chcalendar.job;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class FcmPushNotificationJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String token = context.getMergedJobDataMap().getString("fcmToken");
        String title = context.getMergedJobDataMap().getString("title");
        String body = context.getMergedJobDataMap().getString("body");
        String url = context.getMergedJobDataMap().getString("url");

        Message message = Message.builder()
                .setToken(token)
                .putData("title", title != null ? title : "")
                .putData("body", body != null ? body : "")
                .putData("url", url != null ? url : "")
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("푸시 알림 전송 성공: " + response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JobExecutionException("FCM 푸시 알림 전송 실패: " + e.getMessage(), e);
        }
    }
}

