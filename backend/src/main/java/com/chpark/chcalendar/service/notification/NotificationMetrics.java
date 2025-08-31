package com.chpark.chcalendar.service.notification;

import io.micrometer.core.instrument.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class NotificationMetrics {

    private final MeterRegistry registry;

    // counters
    private Counter scheduledCounter;
    private Counter firedCounter;
    private Counter sentCounter;
    private Counter successCounter;
    private Counter failCounter;

    // timers
    private Timer scheduleToFireTimer;
    private Timer fireToSendTimer;
    private Timer fcmRttTimer;

    @PostConstruct
    void init() {
        // counters
        scheduledCounter = Counter.builder("notif.scheduled.total")
                .description("스케줄 등록 수").register(registry);
        firedCounter = Counter.builder("notif.fired.total")
                .description("Quartz Job 실행 시작").register(registry);
        sentCounter = Counter.builder("notif.sent.total")
                .description("FCM 요청 수").tag("channel", "fcm").register(registry);
        successCounter = Counter.builder("notif.success.total")
                .tag("channel", "fcm").register(registry);
        failCounter = Counter.builder("notif.fail.total")
                .tag("channel", "fcm").tag("reason", "unknown").register(registry);

        // timers (퍼센타일 등 설정이 필요하므로 builder로 한 번만 등록)
        scheduleToFireTimer = Timer.builder("latency.schedule_to_fire")
                .publishPercentiles(0.5, 0.95, 0.99).register(registry);
        fireToSendTimer = Timer.builder("latency.fire_to_send")
                .publishPercentiles(0.5, 0.95, 0.99).register(registry);
        fcmRttTimer = Timer.builder("latency.fcm_rtt")
                .publishPercentiles(0.5, 0.95, 0.99).register(registry);
    }

    // API
    public void onScheduled() { scheduledCounter.increment(); }
    public void onFired() { firedCounter.increment(); }
    public void onSent() { sentCounter.increment(); }
    public void onSuccess() { successCounter.increment(); }
    public void onFail(String reason) { failCounter.increment(); }

    public void recordScheduleToFire(Duration d) { scheduleToFireTimer.record(d); }
    public Timer.Sample startFireToSend() { return Timer.start(registry); }
    public void stopFireToSend(Timer.Sample s) { s.stop(fireToSendTimer); }

    public Timer.Sample startFcmRtt() { return Timer.start(registry); }
    public void stopFcmRtt(Timer.Sample s) { s.stop(fcmRttTimer); }
}