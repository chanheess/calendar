package com.chpark.calendar.controller;

import com.chpark.calendar.dto.MailDto;
import com.chpark.calendar.service.MailService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    //이메일 전송
    @PostMapping("/auth/email")
    public ResponseEntity<?> sendEmail(@NotNull @RequestParam(value = "mail") String mail) {
        String subject = "회원가입 인증 메일입니다.";
        Random random = new Random();
        int code = random.nextInt(9000) + 1000;
        String text = "인증 코드는 " + code + "입니다.";

        mailService.send(mail, subject, text, code);
        return new ResponseEntity<>("이메일 전송 성공", HttpStatus.OK);
    }

    //이메일 인증
    @PostMapping("/auth/email/verify")
    public ResponseEntity<?> verifyEmail(@Validated @RequestBody MailDto mailDto) {
        mailService.verificationEmail(mailDto);
        return new ResponseEntity<>("이메일 인증 성공", HttpStatus.OK);
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/auth/redis-test")
    public String redisTest() {
        redisTemplate.opsForValue().set("testKey", "testValue", 10, TimeUnit.SECONDS);
        return redisTemplate.opsForValue().get("testKey");
    }
}
