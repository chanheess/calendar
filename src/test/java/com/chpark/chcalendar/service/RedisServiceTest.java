package com.chpark.chcalendar.service;

import com.chpark.chcalendar.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
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

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(valueOperations.get("email_request_count:" + email)).thenReturn(null); // 처음 요청
        doNothing().when(mailService).sendMail(anyString(), anyInt());

        // when
        redisService.sendMailAndSaveCode(email);

        // then
        verify(mailService, times(1)).sendMail(eq(email), anyInt());
        verify(valueOperations, times(1)).set(eq(email), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(valueOperations, times(1)).increment(eq("email_request_count:" + email)); // 기본 increment 호출 검증
    }

    @Test
    void sendMailAndSaveCode_existsByEmail() {
        // given
        String email = "test@example.com";
        String verificationCode = "1049";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when then
        assertThatThrownBy(() -> redisService.sendMailAndSaveCode(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 해당 \"이메일\"을 가진 사용자가 존재합니다.");
    }

    @Test
    void sendMailAndSaveCode_verificationCount() {
        // given
        String email = "test@example.com";
        String verificationCode = "1049";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(valueOperations.get("email_request_count:" + email)).thenReturn("5");


        // when then
        assertThatThrownBy(() -> redisService.sendMailAndSaveCode(email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이메일 인증 요청 5번 초과로 24시간 동안 이메일 인증 요청을 할 수 없습니다.");
    }
}