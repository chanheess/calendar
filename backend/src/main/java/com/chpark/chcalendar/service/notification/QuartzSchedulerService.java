package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.job.FcmPushNotificationJob;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

@Service
public class QuartzSchedulerService {

    @Autowired
    private Scheduler scheduler;

    private static final Logger log = LoggerFactory.getLogger(QuartzSchedulerService.class);

    @Transactional
    public void createFcmPushNotification(String jobId, String fcmToken, String userPlatformKey,
                                          String title, String body, String url, LocalDateTime scheduledTime)
            throws SchedulerException {
        // 과거 시간인지 확인
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

        JobDetail jobDetail = JobBuilder.newJob(FcmPushNotificationJob.class)
                .withIdentity(jobId, userPlatformKey)
                .usingJobData(jobDataMap)
                .build();

        Date triggerTime = Date.from(scheduledTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());
        log.info("Trigger time set for: {}", triggerTime);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId, userPlatformKey)
                .startAt(triggerTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("Job scheduled successfully: jobId={}, triggerKey={}", jobId, trigger.getKey());
    }

    @Transactional
    public void updateFcmPushNotification(String jobId, String userPlatformKey, LocalDateTime newScheduledTime) throws SchedulerException {
        // 과거 시간인지 확인
        if (newScheduledTime.isBefore(LocalDateTime.now())) {
            return;
        }
        
        TriggerKey triggerKey = TriggerKey.triggerKey(jobId, userPlatformKey);
        
        try {
            // 기존 트리거가 존재하면 수정
            if (scheduler.checkExists(triggerKey)) {
                Date newTriggerTime = Date.from(newScheduledTime.atZone(ZoneId.systemDefault()).toInstant());
                Trigger newTrigger = TriggerBuilder.newTrigger()
                        .withIdentity(jobId, userPlatformKey)
                        .startAt(newTriggerTime)
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                        .build();
                scheduler.rescheduleJob(triggerKey, newTrigger);
                log.info("Updated existing notification: jobId={}, userPlatformKey={}, newTime={}", 
                        jobId, userPlatformKey, newScheduledTime);
            } else {
                //상위 계층에서 create 해주도록 처리
                throw new SchedulerException("Previous notification expired. Please recreate the notification.");
            }
        } catch (SchedulerException e) {
            throw e;
        }
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


