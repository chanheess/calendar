package com.chpark.chcalendar.service;

import com.chpark.chcalendar.dto.EmailDto;
import com.chpark.chcalendar.exception.authority.EmailAuthorityException;
import com.chpark.chcalendar.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
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

        checkEmailRequestCount(emailDto.getEmail());

        // 6자리 알파벳+숫자 코드 생성
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

        mailService.sendMail(emailDto, code);

        saveVerificationCode(emailDto.getEmail(), code);
        increaseEmailRequestCount(emailDto.getEmail());
    }

    //이메일 인증
    public void verificationEmail(String email, String emailCode) {
        String savedCode = redisTemplate.opsForValue().get(email);

        if (savedCode == null) {
            throw new EmailAuthorityException("인증 시간이 초과되었습니다. 다시 시도해주세요.");
        }
        if (!emailCode.equals(savedCode)) {
            throw new EmailAuthorityException("이메일 인증 실패");
        }

        // 인증 성공 시 인증 코드 및 이메일 인증 요청 카운트 삭제
        deleteVerificationData(email);
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
            redisTemplate.expire(key, 30, TimeUnit.MINUTES); // 30분으로 설정
        }
    }

    public void checkEmailRequestCount(String email) {
        String key = "email_request_count:" + email;
        String value = redisTemplate.opsForValue().get(key);
        long count = value != null ? Long.parseLong(value) : 0;

        if (count >= 5) {
            throw new EmailAuthorityException("이메일 인증 요청 5번 초과로 30분 동안 이메일 인증 요청을 할 수 없습니다.");
        }
    }

    // 인증 성공 시 인증 데이터 삭제
    private void deleteVerificationData(String email) {
        // 인증 코드를 삭제
        redisTemplate.delete(email);

        // 이메일 요청 카운트를 삭제
        String key = "email_request_count:" + email;
        redisTemplate.delete(key);
    }
}

