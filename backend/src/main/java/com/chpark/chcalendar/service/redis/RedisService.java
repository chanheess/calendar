package com.chpark.chcalendar.service.redis;

import com.chpark.chcalendar.dto.EmailDto;
import com.chpark.chcalendar.enumClass.RequestType;
import com.chpark.chcalendar.exception.authentication.CountAuthenticationException;
import com.chpark.chcalendar.exception.authentication.EmailAuthenticationException;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.utility.KeyGeneratorUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class RedisService {
    private final MailService mailService;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    private final int maxRequestCount = 5;

    @Transactional
    public void sendMailAndSaveCode(EmailDto emailDto) {
        switch (emailDto.getType()) {
            case REGISTER -> {
                if (userRepository.existsByEmail(emailDto.getEmail())) {
                    throw new IllegalArgumentException("이미 해당 \"이메일\"을 가진 사용자가 존재합니다.");
                }
            }
            case PASSWORD_RESET -> {
                if (!userRepository.existsByEmail(emailDto.getEmail())) {
                    throw new IllegalArgumentException("이메일을 정확하게 입력해주세요.");
                }
            }
        }

        checkRequestCount(emailDto.getType(), emailDto.getEmail());

        String code = KeyGeneratorUtility.generateMailCode(6);

        mailService.sendMail(emailDto, code);

        saveVerificationCode(emailDto.getEmail(), code);
        increaseRequestCount(emailDto.getType(), emailDto.getEmail());
    }

    //이메일 인증
    public void verificationEmail(EmailDto emailDto, String emailCode) {
        String savedCode = redisTemplate.opsForValue().get(emailDto.getEmail());

        if (savedCode == null) {
            throw new EmailAuthenticationException("인증 시간이 초과되었습니다. 다시 시도해주세요.");
        }
        if (!emailCode.equals(savedCode)) {
            throw new EmailAuthenticationException("이메일 인증 실패");
        }

        // 인증 성공 시 인증 코드 및 이메일 인증 요청 카운트 삭제
        deleteVerificationData(emailDto.getType(), emailDto.getEmail());
    }

    //redis에 인증코드 저장
    public void saveVerificationCode(String email, String code) {
        redisTemplate.opsForValue().set(email, code, 5, TimeUnit.MINUTES);
    }

    //이메일 요청 카운트 증가
    public void increaseRequestCount(RequestType requestType, String targetName) {
        String key = getKey(requestType, targetName);
        Long count = redisTemplate.opsForValue().increment(key);

        // null 체크 추가
        if (count == null) {
            count = 0L; // 기본값 설정
        }

        if (count == maxRequestCount) {
            redisTemplate.expire(key, 30, TimeUnit.MINUTES); // 30분으로 설정
        }
    }

    public void checkRequestCount(RequestType requestType, String targetName) {
        if (hasExceededMaxRequestCount(requestType, targetName)) {
            long expireTime = getRemainingMinute(requestType, targetName);

            throw new CountAuthenticationException(
                String.format("%s 인증 요청 %d번 초과로 %d분 동안 %s 인증 요청을 할 수 없습니다.",
                    requestType.getMessage(), maxRequestCount, expireTime, requestType.getMessage())
            );
        }
    }

    // 인증 성공 시 인증 데이터 삭제
    public void deleteVerificationData(RequestType requestType, String targetName) {
        // 인증 코드를 삭제
        redisTemplate.delete(targetName);

        // 이메일 요청 카운트를 삭제
        redisTemplate.delete(getKey(requestType, targetName));
    }

    public String getKey(RequestType requestType, String targetName) {
        return requestType.getCode() + ":" + targetName;
    }

    public long getRemainingMinute(RequestType requestType, String targetName) {
        String key = getKey(requestType, targetName);
        Long expireTime = redisTemplate.getExpire(key, TimeUnit.MINUTES);

        if (expireTime == null || expireTime <= 0) {
            return 0;
        }

        return expireTime;
    }

    public boolean hasExceededMaxRequestCount(RequestType requestType, String targetName) {
        String value = redisTemplate.opsForValue().get(getKey(requestType, targetName));
        int count = value != null ? Integer.parseInt(value) : 0;

        return count >= maxRequestCount;
    }

    public String getRequestCount(RequestType requestType, String targetName) {
        String value = redisTemplate.opsForValue().get(getKey(requestType, targetName));
        int count = value != null ? Integer.parseInt(value) : 0;

        return count + "/" + maxRequestCount;
    }
}

