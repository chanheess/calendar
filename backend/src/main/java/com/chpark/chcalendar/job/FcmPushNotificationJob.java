package com.chpark.chcalendar.job;

import com.chpark.chcalendar.metric.NotificationMetrics;
import com.chpark.chcalendar.service.notification.FcmSender;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class FcmPushNotificationJob implements Job {

    private final NotificationMetrics metrics;
    private final FcmSender fcmSender;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        var data = jobExecutionContext.getMergedJobDataMap();

        // schedule→fire 지연 기록 + fired 카운트
        recordScheduleToFire(jobExecutionContext);
        metrics.onFired();

        // 메시지 작성
        long scheduledAt = asLong(data, "scheduledAt", 0L);
        long firedAt     = fireTimeMs(jobExecutionContext);
        String notifyId  = jobKeyOrRandom(jobExecutionContext);
        long sentAt      = System.currentTimeMillis();

        Message msg = buildFcmMessage(data, notifyId, scheduledAt, firedAt, sentAt);

        // fire→send 타이머 + 전송/성공/실패 카운트
        var t = metrics.startFireToSend();
        metrics.onSent();
        try {
            var future = fcmSender
                    .sendAsync(msg)
                    .orTimeout(10, TimeUnit.SECONDS);
            String messageId = future.join();
            metrics.onSuccess();
        } catch (CompletionException ce) {
            Throwable cause = (ce.getCause() != null) ? ce.getCause() : ce;
            metrics.onFail(cause.getClass().getSimpleName());
            throw new JobExecutionException("FCM 전송 실패: " + cause.getMessage(), cause);
        } catch (Exception e) {
            metrics.onFail(e.getClass().getSimpleName());
            throw new JobExecutionException("FCM 전송 실패: " + e.getMessage(), e);
        } finally {
            metrics.stopFireToSend(t);
        }
    }


    private static long asLong(Map<?,?> map, String key, long def) {
        if (!map.containsKey(key)) return def;
        Object v = map.get(key);
        try {
            return (v instanceof Number) ? ((Number) v).longValue() : Long.parseLong(String.valueOf(v));
        } catch (Exception ignore) { return def; }
    }

    private static String jobKeyOrRandom(JobExecutionContext ctx) {
        JobKey key = ctx.getJobDetail().getKey();
        return (key != null ? key.getName() : UUID.randomUUID().toString());
    }

    private static long fireTimeMs(JobExecutionContext ctx) {
        Date fire = ctx.getFireTime();
        return (fire != null) ? fire.getTime() : System.currentTimeMillis();
    }

    private void recordScheduleToFire(JobExecutionContext ctx) {
        Date sched = ctx.getScheduledFireTime();
        Date fire  = ctx.getFireTime();
        if (sched == null || fire == null) return;
        Duration d = Duration.between(sched.toInstant(), fire.toInstant());
        if (!d.isNegative()) metrics.recordScheduleToFire(d);
    }

    private static Message buildFcmMessage(JobDataMap data,
                                           String notifyId, long scheduledAt, long firedAt, long sentAt) {
        String token = data.getString("fcmToken");
        String title = data.getString("title");
        String body  = data.getString("body");
        String url   = data.getString("url");
        return Message.builder()
                .setToken(token)
                .putData("title", nvl(title))
                .putData("body",  nvl(body))
                .putData("url",   nvl(url))
                .putData("notifyId",    notifyId)
                .putData("scheduledAt", String.valueOf(scheduledAt))
                .putData("firedAt",     String.valueOf(firedAt))
                .putData("sentAt",      String.valueOf(sentAt))
                .build();
    }

    private static String nvl(String s) { return (s == null ? "" : s); }
}