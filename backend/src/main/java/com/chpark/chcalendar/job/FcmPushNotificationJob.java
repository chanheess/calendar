package com.chpark.chcalendar.job;

import com.chpark.chcalendar.service.notification.FcmSender;
import com.chpark.chcalendar.service.notification.NotificationMetrics;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.DisallowConcurrentExecution;

import java.time.Duration;
import java.util.Date;

@RequiredArgsConstructor
@DisallowConcurrentExecution
public class FcmPushNotificationJob implements Job {

    private final NotificationMetrics metrics;
    private final FcmSender fcmSender;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String token = context.getMergedJobDataMap().getString("fcmToken");
        String title = context.getMergedJobDataMap().getString("title");
        String body  = context.getMergedJobDataMap().getString("body");
        String url   = context.getMergedJobDataMap().getString("url");

        Date scheduledFire = context.getScheduledFireTime(); // 예정 시각
        Date actualFire = context.getFireTime();          // 실제 실행 시각
        if (scheduledFire != null && actualFire != null) {
            Duration delay = Duration.between(scheduledFire.toInstant(), actualFire.toInstant());
            if (!delay.isNegative()) {
                metrics.recordScheduleToFire(delay);
            }
        }

        metrics.onFired();

        Message message = Message.builder()
                .setToken(token)
                .putData("title", title != null ? title : "")
                .putData("body", body != null ? body : "")
                .putData("url", url != null ? url : "")
                .build();

        var fireToSendSample = metrics.startFireToSend();
        var fcmRttSample     = metrics.startFcmRtt();

        try {
            metrics.onSent();
            String messageId = fcmSender.send(message);

            // 성공 카운트 & 타이머 종료
            metrics.onSuccess();
            metrics.stopFcmRtt(fcmRttSample);
            metrics.stopFireToSend(fireToSendSample);

        } catch (Exception e) {
            metrics.onFail(e.getClass().getSimpleName());
            try { metrics.stopFcmRtt(fcmRttSample); } catch (Exception ignore) {}
            try { metrics.stopFireToSend(fireToSendSample); } catch (Exception ignore) {}
            throw new JobExecutionException("FCM 전송 실패: " + e.getMessage(), e);
        }
    }
}