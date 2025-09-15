package com.chpark.chcalendar.controller.notification;

import com.chpark.chcalendar.metric.NotificationMetrics;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationAckController {

    private final StringRedisTemplate redis;
    private final NotificationMetrics metrics;

    @Getter
    @ToString
    public static class AckRequest {
        @NotBlank private String notifyId;
        private Long receivedAt;   // SW 수신 시각(ms, optional)
        private Long displayedAt;  // UI 표시 시각(ms, optional)
    }

    @PostMapping("/ack")
    public ResponseEntity<Void> ack(@RequestBody @Validated AckRequest dto) {
        final String key = "notif:sch:" + dto.getNotifyId();

        String scheduledAtStr = getAndDelete(key);
        if (scheduledAtStr == null) {
            // 상관키 만료 or 잘못된 notifyId
            metrics.onClientAckOrphan();
            log.debug("[ACK] orphan notifyId={}, receivedAt={}, displayedAt={}",
                    dto.getNotifyId(), dto.getReceivedAt(), dto.getDisplayedAt());
            return ResponseEntity.ok().build();
        }

        long scheduledAt;
        try {
            scheduledAt = Long.parseLong(scheduledAtStr);
        } catch (NumberFormatException nfe) {
            metrics.onClientAckOrphan();
            log.warn("[ACK] invalid scheduledAt in redis. notifyId={}, raw={}", dto.getNotifyId(), scheduledAtStr);
            return ResponseEntity.ok().build();
        }

        long nowServer = System.currentTimeMillis();
        long e2eClientMillis = nowServer - scheduledAt; // 예약 → ACK(서버 수신)까지
        if (e2eClientMillis >= 0) {
            metrics.recordE2EClient(Duration.ofMillis(e2eClientMillis));
        }
        metrics.onClientAck();

        if (dto.getReceivedAt() != null) {
            long clientDelta = dto.getReceivedAt() - scheduledAt; // 클라 시계 기준 E2E(참고용)
            log.debug("[ACK] ok notifyId={}, e2eClient(server)={}ms, e2eClient(client)={}ms (sched={}, recvClient={}, nowServer={})",
                    dto.getNotifyId(), e2eClientMillis, clientDelta, scheduledAt, dto.getReceivedAt(), nowServer);
        } else {
            log.debug("[ACK] ok notifyId={}, e2eClient(server)={}ms (sched={}, nowServer={})",
                    dto.getNotifyId(), e2eClientMillis, scheduledAt, nowServer);
        }

        return ResponseEntity.ok().build();
    }

    private String getAndDelete(String key) {
        try {
            return redis.opsForValue().getAndDelete(key);
        } catch (NoSuchMethodError | UnsupportedOperationException e) {
            String v = redis.opsForValue().get(key);
            if (v != null) {
                redis.delete(key);
            }
            return v;
        }
    }
}