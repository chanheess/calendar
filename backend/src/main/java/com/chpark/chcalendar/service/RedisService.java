package com.chpark.chcalendar.service;

import com.chpark.chcalendar.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class RedisService {
    private final MailService mailService;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    @Transactional
    public void sendMailAndSaveCode(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 해당 \"이메일\"을 가진 사용자가 존재합니다.");
        }

        long count = getEmailRequestCount(email);
        if (count >= 5) {
            throw new RuntimeException("이메일 인증 요청 5번 초과로 24시간 동안 이메일 인증 요청을 할 수 없습니다.");
        }

        Random random = new Random();
        int code = random.nextInt(9000) + 1000;

        mailService.sendMail(email, code);

        saveVerificationCode(email, String.valueOf(code));
        increaseEmailRequestCount(email);
    }

    //이메일 인증
    public void verificationEmail(String email, String emailCode) {
        String savedCode = redisTemplate.opsForValue().get(email);

        if (savedCode == null) {
            throw new IllegalArgumentException("인증 시간이 초과되었습니다. 다시 시도해주세요.");
        }
        if (!emailCode.equals(savedCode)) {
            throw new IllegalArgumentException("이메일 인증 실패");
        }
    }

    //redis에 인증코드 저장
    public void saveVerificationCode(String email, String code) {
        redisTemplate.opsForValue().set(email, code, 5, TimeUnit.MINUTES);
    }

    //이메일 요청 카운트 증가
    public void increaseEmailRequestCount(String email) {
        String key = "email_request_count:" + email;
        Long count = redisTemplate.opsForValue().increment(key);

        // null 체크 추가
        if (count == null) {
            count = 0L; // 기본값 설정
        }

        if (count == 5) {
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
    }

    //이메일 요청 카운트 가져오기
    public long getEmailRequestCount(String email) {
        String key = "email_request_count:" + email;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0;
    }
}
