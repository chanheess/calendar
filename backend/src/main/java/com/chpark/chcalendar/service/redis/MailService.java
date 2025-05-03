package com.chpark.chcalendar.service.redis;

import com.chpark.chcalendar.dto.EmailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;

    //이메일 전송
    @Async
    public void sendMail(EmailDto emailDto, String code) {
        String subject = emailDto.getType().getMessage() + " 인증 메일입니다.";
        String text = "인증 코드는 " + code + " 입니다.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailDto.getEmail());
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

}