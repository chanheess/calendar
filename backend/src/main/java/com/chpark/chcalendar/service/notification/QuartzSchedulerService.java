package com.chpark.chcalendar.service.notification;

import com.chpark.chcalendar.job.FcmPushNotificationJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class QuartzSchedulerService {

    @Autowired
    private Scheduler scheduler;

    @Transactional
    public void createFcmPushNotification(String jobId, String fcmToken, String title, String body, String url, LocalDateTime scheduledTime) throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("fcmToken", fcmToken);
        jobDataMap.put("title", title);
        jobDataMap.put("body", body);
        jobDataMap.put("url", url);

        JobDetail jobDetail = JobBuilder.newJob(FcmPushNotificationJob.class)
                .withIdentity(jobId)
                .usingJobData(jobDataMap)
                .build();

        Date triggerTime = Date.from(scheduledTime.atZone(ZoneId.systemDefault()).toInstant());

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId)
                .startAt(triggerTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    @Transactional
    public void updateFcmPushNotification(String jobId, LocalDateTime newScheduledTime) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobId);
        Date newTriggerTime = Date.from(newScheduledTime.atZone(ZoneId.systemDefault()).toInstant());
        Trigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId)
                .startAt(newTriggerTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();
        scheduler.rescheduleJob(triggerKey, newTrigger);
    }

    @Transactional
    public void deleteFcmPushNotification(String jobId) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobId);
        scheduler.deleteJob(jobKey);
    }

}


