package com.chpark.calendar.controller;

import com.chpark.calendar.service.RedisService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    //이메일 전송
    @PostMapping("/auth/mail/{email}")
    public ResponseEntity<?> sendMail(@NotNull @PathVariable(value = "email") String email) {
        try {
            redisService.sendMailAndSaveCode(email);
            return new ResponseEntity<>("이메일 전송 성공", HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
