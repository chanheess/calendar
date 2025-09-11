package com.chpark.chcalendar.controller.notification;

import com.chpark.chcalendar.metric.NotificationMetrics;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
class NotificationAckController {

    private final StringRedisTemplate redis;
    private final NotificationMetrics metrics;

    // DTO 예시 (epoch millis 기준)
    public record AckDto(String notifyId, long receivedAt, Long displayedAt, String sig) {}

    @PostMapping("/ack")
    public ResponseEntity<Void> ack(@RequestBody AckDto dto, HttpServletRequest req) {
        // (옵션) 위조 방지: HMAC 서명 검증
        if (!verifySignature(dto)) return ResponseEntity.status(401).build();

        String key = "notif:sch:" + dto.notifyId();
        String scheduledAtStr = redis.opsForValue().get(key);
        if (scheduledAtStr == null) {
            // 상관키 만료 or 알 수 없는 notifyId
            metrics.onClientAckOrphan();
            return ResponseEntity.ok().build();
        }

        long scheduledAt = Long.parseLong(scheduledAtStr);
        long receivedAt  = dto.receivedAt();

        // 단말 관점 지표
        metrics.recordE2EClient(Duration.ofMillis(receivedAt - scheduledAt)); // 예약→단말 수신

        metrics.onClientAck();
        redis.delete(key);

        return ResponseEntity.ok().build();
    }

    private boolean verifySignature(AckDto dto) {
        // HMAC-SHA256(notifyId|receivedAt|displayedAt?, secret) 같은 방식 권장
        return true;
    }
}
