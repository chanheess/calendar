package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.dto.EmailDto;
import com.chpark.chcalendar.service.RedisService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    @PostMapping("/auth/mail")
    public ResponseEntity<String> sendAuthenticationMail(@Validated @RequestBody EmailDto email) {
        redisService.sendMailAndSaveCode(email);

        return ResponseEntity.ok("이메일 전송 성공");
    }
}
