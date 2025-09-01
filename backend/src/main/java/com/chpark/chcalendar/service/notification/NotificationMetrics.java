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

    // counters (기본 축약본)
    private Counter scheduledCounter;
    private Counter firedCounter;
    private Counter sentCounter;
    private Counter successCounter;
    private Counter failCounterDefault; // reason 미지정시/호환용

    // timers
    private Timer scheduleToFireTimer;
    private Timer fireToSendTimer;
    private Timer fcmRttTimer;

    @PostConstruct
    void init() {
        // ===== Counters
        scheduledCounter = Counter.builder("notif.scheduled.total")
                .description("스케줄 등록 수")
                .register(registry);

        firedCounter = Counter.builder("notif.fired.total")
                .description("Quartz Job 실행 시작")
                .register(registry);

        sentCounter = Counter.builder("notif.sent.total")
                .description("FCM 요청 수")
                .tag("channel", "fcm")
                .register(registry);

        successCounter = Counter.builder("notif.success.total")
                .tag("channel", "fcm")
                .register(registry);

        // 기본 fail 카운터(호환용, reason 태그 고정)
        failCounterDefault = Counter.builder("notif.fail.total")
                .tag("channel", "fcm")
                .tag("reason", "unknown")
                .register(registry);

        // ===== Timers
        // 히스토그램 + SLO 버킷을 켜고, 퍼센타일 시계열도 제공 (원치 않으면 publishPercentiles(...)만 주석 처리)
        scheduleToFireTimer = Timer.builder("latency.schedule_to_fire")
                .description("스케줄 등록시각 → Quartz 실행 사이 지연")
                .publishPercentileHistogram(true)               // _bucket/_count/_sum 노출
                .serviceLevelObjectives(
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(5),
                        Duration.ofSeconds(10),
                        Duration.ofSeconds(20)
                )
                .publishPercentiles(0.5, 0.95, 0.99)            // {quantile=...} 시계열 (불필요하면 제거)
                .register(registry);

        fireToSendTimer = Timer.builder("latency.fire_to_send")
                .description("Quartz 실행 시작 → FCM 요청 발행 사이 지연")
                .publishPercentileHistogram(true)
                .serviceLevelObjectives(
                        Duration.ofMillis(200),
                        Duration.ofMillis(500),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(5)
                )
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        fcmRttTimer = Timer.builder("latency.fcm_rtt")
                .description("FCM 요청 → 응답(동기 전송) 왕복 지연")
                .publishPercentileHistogram(true)
                .serviceLevelObjectives(
                        Duration.ofMillis(200),
                        Duration.ofMillis(500),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(5)
                )
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    // ===== API (외부 호출 시그니처는 변경 없음)
    public void onScheduled() { scheduledCounter.increment(); }
    public void onFired()     { firedCounter.increment(); }
    public void onSent()      { sentCounter.increment(); }
    public void onSuccess()   { successCounter.increment(); }

    /**
     * 실패 카운트: 기존 기본 카운터(unknown)도 올리고,
     *             추가로 reason 태그별 카운터를 동시 증가시켜서 대시보드에서 이유별 집계 가능.
     */
    public void onFail(String reason) {
        failCounterDefault.increment(); // 호환/총합 유지
        if (reason == null || reason.isBlank()) {
            reason = "unknown";
        }
        Counter reasoned = registry.counter("notif.fail.total",
                "channel", "fcm",
                "reason", reason);
        reasoned.increment();
    }

    // 지연 기록
    public void recordScheduleToFire(Duration d) { scheduleToFireTimer.record(d); }

    public Timer.Sample startFireToSend() { return Timer.start(registry); }
    public void stopFireToSend(Timer.Sample s) { if (s != null) s.stop(fireToSendTimer); }

    public Timer.Sample startFcmRtt() { return Timer.start(registry); }
    public void stopFcmRtt(Timer.Sample s) { if (s != null) s.stop(fcmRttTimer); }
}