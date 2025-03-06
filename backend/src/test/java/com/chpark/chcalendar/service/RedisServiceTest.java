package com.chpark.chcalendar.service;

import com.chpark.chcalendar.dto.EmailDto;
import com.chpark.chcalendar.enumClass.RequestType;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.service.redis.MailService;
import com.chpark.chcalendar.service.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private MailService mailService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisService redisService;

    @Test
    void sendMailAndSaveCode() {
        // given
        String email = "test@example.com";
        String verificationCode = "1049";
        EmailDto emailDto = new EmailDto(email, RequestType.REGISTER);
        String increaseKey = emailDto.getType().getCode() + ":" + email;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(valueOperations.get(increaseKey)).thenReturn(null); // 처음 요청
        doNothing().when(mailService).sendMail(any(EmailDto.class), anyString());

        // when
        redisService.sendMailAndSaveCode(emailDto);

        // then
        verify(mailService, times(1)).sendMail(eq(emailDto), anyString());
        verify(valueOperations, times(1)).set(eq(email), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(valueOperations, times(1)).increment(eq(increaseKey)); // 기본 increment 호출 검증
    }

    @Test
    void sendMailAndSaveCode_existsByEmail() {
        // given
        String email = "test@example.com";
        String verificationCode = "1049";
        EmailDto emailDto = new EmailDto(email, RequestType.REGISTER);

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when then
        assertThatThrownBy(() -> redisService.sendMailAndSaveCode(emailDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 해당 \"이메일\"을 가진 사용자가 존재합니다.");
    }

    @Test
    void sendMailAndSaveCode_verificationCount() {
        // given
        String email = "test@example.com";
        String verificationCode = "1049";
        EmailDto emailDto = new EmailDto(email, RequestType.REGISTER);
        String increaseKey = emailDto.getType().getCode() + ":" + email;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(valueOperations.get(increaseKey)).thenReturn("5");
        when(redisService.getRemainingMinute(emailDto.getType(), emailDto.getEmail())).thenReturn(30L);

        // when then
        assertThatThrownBy(() -> redisService.sendMailAndSaveCode(emailDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(String.format("%s 인증 요청 5번 초과로 30분 동안 %s 인증 요청을 할 수 없습니다.",
                        emailDto.getType().getMessage(), emailDto.getType().getMessage()));
    }
}