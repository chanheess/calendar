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
    private Counter failTotalCounter;
    private Counter clientAckOkCounter;
    private Counter clientAckOrphanCounter;

    // Timers
    private Timer scheduleToFireTimer;  // Quartz 스케줄 지연
    private Timer fireToSendTimer;      // Job 실행 → sendAsync 호출
    private Timer fcmRttTimer;          // sendAsync → FCM 응답
    private Timer e2eClientTimer;       // 예약 → 단말 ACK

    @PostConstruct
    void init() {
        initCounters();
        initTimers();
    }

    private void initCounters() {
        firedCounter = Counter.builder("notif.fired.total")
                .description("Quartz Job 실행 시작 횟수")
                .tag("channel", "fcm")
                .register(registry);

        sentCounter = Counter.builder("notif.sent.total")
                .description("FCM 요청 수")
                .tag("channel", "fcm")
                .register(registry);

        successCounter = Counter.builder("notif.success.total")
                .description("FCM 요청 성공 수")
                .tag("channel", "fcm")
                .register(registry);

        failTotalCounter = Counter.builder("notif.fail.total")
                .description("FCM 요청 실패 총합")
                .tag("channel", "fcm")
                .tag("reason", "all")
                .register(registry);

        clientAckOkCounter = Counter.builder("notif.client.ack.total")
                .description("클라이언트 ACK (정상 상관)")
                .tag("status", "ok")
                .register(registry);

        clientAckOrphanCounter = Counter.builder("notif.client.ack.total")
                .description("클라이언트 ACK (상관키 orphan)")
                .tag("status", "orphan")
                .register(registry);
    }

    private void initTimers() {
        scheduleToFireTimer = Timer.builder("latency.schedule_to_fire")
                .description("스케줄 등록시각 → Quartz 실행 지연")
                .publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        fireToSendTimer = Timer.builder("latency.fire_to_send")
                .description("Quartz 실행 시작 → FCM 요청 발행 지연")
                .publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        fcmRttTimer = Timer.builder("latency.fcm_rtt")
                .description("FCM 요청 → FCM 응답 RTT")
                .publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        e2eClientTimer = Timer.builder("latency.e2e_client")
                .description("예약 시각 → 단말 ACK까지의 지연")
                .publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    // Counter API
    public void onFired() { firedCounter.increment(); }
    public void onSent() { sentCounter.increment(); }
    public void onSuccess() { successCounter.increment(); }

    public void onFail(String reason) {
        failTotalCounter.increment();
        if (reason == null || reason.isBlank()) reason = "unknown";
        registry.counter("notif.fail.total", "channel", "fcm", "reason", reason).increment();
    }

    public void onClientAck() { clientAckOkCounter.increment(); }
    public void onClientAckOrphan() { clientAckOrphanCounter.increment(); }

    // Timer API
    public void recordScheduleToFire(Duration d) {
        if (d != null && !d.isNegative()) scheduleToFireTimer.record(d);
    }

    public Timer.Sample startFireToSend() { return Timer.start(registry); }
    public void stopFireToSend(Timer.Sample s) { if (s != null) s.stop(fireToSendTimer); }

    public Timer.Sample startFcmRtt() { return Timer.start(registry); }
    public void stopFcmRtt(Timer.Sample s) { if (s != null) s.stop(fcmRttTimer); }

    public void recordE2EClient(Duration d) {
        if (d != null && !d.isNegative()) e2eClientTimer.record(d);
    }
}