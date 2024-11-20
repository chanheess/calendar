package com.chpark.calendar.controller;

import com.chpark.calendar.service.MailService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    //이메일 전송
    @PostMapping("/auth/mail/{email}")
    public ResponseEntity<?> sendMail(@NotNull @PathVariable(value = "email") String email) {
        try {
            mailService.sendMail(email);
            return new ResponseEntity<>("이메일 전송 성공", HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
