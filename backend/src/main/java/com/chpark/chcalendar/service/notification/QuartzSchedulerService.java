package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.job.FcmPushNotificationJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class QuartzSchedulerService {

    private final Scheduler scheduler;
    private final NotificationMetrics metrics;

    private static final Logger log = LoggerFactory.getLogger(QuartzSchedulerService.class);

    @Transactional
    public void createFcmPushNotification(String jobId, String fcmToken, String userPlatformKey,
                                          String title, String body, String url, LocalDateTime scheduledTime)
            throws SchedulerException {
        if (scheduledTime.isBefore(LocalDateTime.now())) {
            return;
        }
        
        log.info("Creating FCM Push Notification: jobId={}, userPlatformKey={}, scheduledTime={}",
                jobId, userPlatformKey, scheduledTime);

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("fcmToken", fcmToken);
        jobDataMap.put("userPlatformKey", userPlatformKey);
        jobDataMap.put("title", title);
        jobDataMap.put("body", body);
        jobDataMap.put("url", url);

        long scheduledAt = scheduledTime.atZone(ZoneId.of("Asia/Seoul"))
                .toInstant().toEpochMilli();
        jobDataMap.put("scheduledAt", scheduledAt);

        JobDetail jobDetail = JobBuilder.newJob(FcmPushNotificationJob.class)
                .withIdentity(jobId, userPlatformKey)
                .usingJobData(jobDataMap)
                .requestRecovery(true)
                .storeDurably(false)
                .build();

        Date triggerTime = Date.from(scheduledTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId, userPlatformKey)
                .startAt(triggerTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        metrics.onScheduled();
    }

    @Transactional
    public void updateFcmPushNotification(String jobId, String fcmToken, String userPlatformKey,
                                          String title, String body, String url,
                                          LocalDateTime newScheduledTime) throws SchedulerException {
        if (newScheduledTime.isBefore(LocalDateTime.now())) {
            return;
        }

        JobKey jobKey = JobKey.jobKey(jobId, userPlatformKey);
        if (!scheduler.checkExists(jobKey)) {
            throw new SchedulerException("Previous notification expired. Please recreate the notification.");
        }

        JobDetail oldJob = scheduler.getJobDetail(jobKey);
        JobDataMap newData = new JobDataMap(oldJob.getJobDataMap());

        if (title != null) newData.put("title", title);
        if (body  != null) newData.put("body",  body);
        if (url   != null) newData.put("url",   url);
        if (fcmToken != null) newData.put("fcmToken", fcmToken);

        long scheduledAt = newScheduledTime.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
        newData.put("scheduledAt", scheduledAt);

        JobDetail newJob = oldJob.getJobBuilder()
                .usingJobData(newData)
                .build();

        Date newTriggerTime = Date.from(newScheduledTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());
        TriggerKey triggerKey = TriggerKey.triggerKey(jobId, userPlatformKey);

        Trigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(jobKey)
                .startAt(newTriggerTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();

        scheduler.addJob(newJob, true, true);
        scheduler.rescheduleJob(triggerKey, newTrigger);

        log.info("Notification updated: jobId={}, userPlatformKey={}, newTime={}",
                jobId, userPlatformKey, newScheduledTime);
    }

    @Transactional
    public void deleteFcmPushNotification(String jobId, String userPlatformKey) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobId, userPlatformKey);
        
        // 잡이 존재하는지 확인
        if (!scheduler.checkExists(jobKey)) {
            return; // 이미 만료되어 삭제된 상태
        }
        
        scheduler.deleteJob(jobKey);
        log.info("Deleted notification: jobId={}, userPlatformKey={}", jobId, userPlatformKey);
    }

    @Transactional
    public void changeTokenInPushNotification(String userPlatformKey, String newFcmToken) throws SchedulerException {
        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(userPlatformKey));

        for (JobKey jobKey : jobKeys) {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            JobDataMap dataMap = jobDetail.getJobDataMap();
            dataMap.put("fcmToken", newFcmToken);
            JobDetail newJobDetail = jobDetail.getJobBuilder()
                    .usingJobData(dataMap)
                    .build();

            scheduler.addJob(newJobDetail, true, true);
        }
    }

}


