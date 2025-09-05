package com.chpark.chcalendar.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class NotificationMetrics {

    private final MeterRegistry registry;

    // Counters
    private Counter firedCounter;
    private Counter sentCounter;
    private Counter successCounter;
    private Counter failTotalCounter;   // 총 실패

    // Timers
    private Timer scheduleToFireTimer;  // scheduledFire -> fireTime
    private Timer fireToSendTimer;      // job execute 시작 -> FCM send 호출

    @PostConstruct
    void init() {
        // timers
        scheduleToFireTimer = Timer.builder("latency.schedule_to_fire")
                .description("스케줄 등록시각 → Quartz 실행 사이 지연")
                .publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        fireToSendTimer = Timer.builder("latency.fire_to_send")
                .description("Quartz 실행 시작 → FCM 요청 발행 사이 지연")
                .publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        // counters
        firedCounter    = Counter.builder("notif.fired.total")
                .description("Quartz Job 실행 시작")
                .tag("channel", "fcm")
                .register(registry);

        sentCounter     = Counter.builder("notif.sent.total")
                .tag("channel", "fcm")
                .description("FCM 요청 수")
                .register(registry);

        successCounter  = Counter.builder("notif.success.total")
                .tag("channel", "fcm")
                .description("FCM 요청 성공 수")
                .register(registry);

        failTotalCounter = Counter.builder("notif.fail.total")
                .tag("channel", "fcm")
                .description("FCM 요청 실패 총합")
                .tag("reason", "all")
                .register(registry);
    }

    // API
    public void onFired()  { firedCounter.increment(); }
    public void onSent()   { sentCounter.increment(); }
    public void onSuccess(){ successCounter.increment(); }

    /** 실패 총합 + 이유별 카운트 둘 다 올림 */
    public void onFail(String reason) {
        failTotalCounter.increment();
        if (reason == null || reason.isBlank()) reason = "unknown";
        registry.counter("notif.fail.total", "reason", reason).increment();
    }

    // Timers
    public void recordScheduleToFire(Duration d) {
        scheduleToFireTimer.record(d);
    }

    public Timer.Sample startFireToSend() {
        return Timer.start(registry);
    }

    public void stopFireToSend(Timer.Sample s) {
        if (s != null) s.stop(fireToSendTimer);
    }
}