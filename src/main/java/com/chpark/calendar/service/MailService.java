package com.chpark.calendar.service;

import com.chpark.calendar.dto.MailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate redisTemplate;

    //이메일 전송
    public void send(String mail, String subject, String text, int code) {
        long count = getEmailRequestCount(mail);
        if (count == 5) {
            throw new RuntimeException("이메일 인증 요청 5번 초과로 24시간 동안 이메일 인증 요청을 할 수 없습니다.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mail);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
        saveVerificationCode(mail, String.valueOf(code)); //인증 코드 저장

        increaseEmailRequestCount(mail); // 이메일을 보낸 후 요청 횟수를 증가
    }

    //이메일 인증
    public void verificationEmail(MailDto mailDto) {
        String savedCode = redisTemplate.opsForValue().get(mailDto.getEmail());

        if (!mailDto.getCode().equals(savedCode)) {
            throw new IllegalArgumentException("이메일 인증 실패");
        }
    }

    //redis에 인증코드 저장
    public void saveVerificationCode(String email, String code) {
        redisTemplate.opsForValue().set(email, code, 1, TimeUnit.MINUTES); //1분 타임아웃
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