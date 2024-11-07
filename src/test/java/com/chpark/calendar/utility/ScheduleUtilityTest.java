package com.chpark.calendar.utility;

import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.when;


class ScheduleUtilityTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
    }

    @Test
    void calculateRepeatCount() {
        //TODO: 추후 작성할 것
    }

    @Test
    void calculateRepeatPlusDate() {
        //TODO: 추후 작성할 것
    }

    @Test
    void validateEmail() {
        // 테스트할 이메일 리스트 설정
        List<String> validEmails = Arrays.asList("test@example.com", "user.name@domain.com");
        List<String> invalidEmails = Arrays.asList("invalid-email", "user@.com");

        // 성공 케이스 테스트 (예외 발생하지 않음)
        for (String email : validEmails) {
            assertThatCode(() -> ScheduleUtility.validateEmail(email))
                    .doesNotThrowAnyException();
        }

        // 실패 케이스 테스트 (예외 발생)
        for (String email : invalidEmails) {
            assertThatThrownBy(() -> ScheduleUtility.validateEmail(email))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email format");
        }
    }

    @Test
    void hashPasswordTest() {
        // PasswordEncoder의 동작 정의 (Mock)
        String rawPassword = "password123!@#";
        String encodedPassword = "encoded_password123";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(passwordEncoder.matches("password123!@3", encodedPassword)).thenReturn(false);

        UserDto.RegisterRequest userRequest = new UserDto.RegisterRequest(
                "email@naver.com",
                rawPassword,
                "test_king"
        );
        UserEntity userEntity = new UserEntity(userRequest, passwordEncoder);

        assertThat(userEntity.checkPassword(rawPassword, passwordEncoder)).isTrue();
        assertThat(userEntity.checkPassword("password123!@3", passwordEncoder)).isFalse();
    }

}