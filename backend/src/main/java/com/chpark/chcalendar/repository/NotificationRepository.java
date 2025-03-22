package com.chpark.chcalendar.repository;

import com.chpark.chcalendar.entity.NotificationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class NotificationRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();

        // SCAN 명령에 사용할 옵션 설정
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern) // 키 패턴 설정
                .count(100)     // 한 번에 검색할 키 수
                .build();

        // Redis SCAN 명령 실행
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            Cursor<byte[]> cursor = connection.scan(options); // SCAN 명령 실행
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next())); // 결과를 Set에 추가
            }
            return null;
        });

        return keys; // 검색된 키 반환
    }

    public void save(NotificationEntity notification) {
        redisTemplate.opsForValue().set(notification.getKey(), notification);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void deletePatten(String pattern) {
        Set<String> keysToDelete = scanKeys(pattern);
        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
    }
}
