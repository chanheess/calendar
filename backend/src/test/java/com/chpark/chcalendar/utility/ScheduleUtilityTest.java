package com.chpark.chcalendar.utility;

import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.enumClass.ScheduleRepeatType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleUtilityTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void calculateRepeatCountTest() {
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 1, 31, 0, 0);

        // 일간 반복
        assertThat(ScheduleUtility.calculateRepeatCount(startDate, endDate, 1, ScheduleRepeatType.DAY))
                .isEqualTo(30);

        // 주간 반복
        assertThat(ScheduleUtility.calculateRepeatCount(startDate, endDate, 1, ScheduleRepeatType.WEEK))
                .isEqualTo(4);

        // 월간 반복
        assertThat(ScheduleUtility.calculateRepeatCount(startDate, endDate, 1, ScheduleRepeatType.MONTH))
                .isEqualTo(0); // (31일 사이에 한 번이니까 반복은 없으므로 0)

        // 연간 반복
        assertThat(ScheduleUtility.calculateRepeatCount(startDate, endDate, 1, ScheduleRepeatType.YEAR))
                .isEqualTo(0); // (같은 해 내 반복이 없으므로 0)

        // 종료일이 없을 때
        assertThat(ScheduleUtility.calculateRepeatCount(startDate, null, 1, ScheduleRepeatType.DAY))
                .isEqualTo(500);
    }

    @Test
    void calculateRepeatPlusDateTest() {
        LocalDateTime date = LocalDateTime.of(2023, 1, 1, 0, 0);

        // 일간 반복 추가 (interval = 10, repeatType = DAYS)
        assertThat(ScheduleUtility.calculateRepeatPlusDate(date, ScheduleRepeatType.DAY, 10))
                .isEqualTo(date.plusDays(10));

        // 주간 반복 추가 (interval = 2, repeatType = WEEKS)
        assertThat(ScheduleUtility.calculateRepeatPlusDate(date, ScheduleRepeatType.WEEK, 2))
                .isEqualTo(date.plusWeeks(2));

        // 월간 반복 추가 (interval = 3, repeatType = MONTHS)
        assertThat(ScheduleUtility.calculateRepeatPlusDate(date, ScheduleRepeatType.MONTH, 3))
                .isEqualTo(date.plusMonths(3));

        // 연간 반복 추가 (interval = 1, repeatType = YEARS)
        assertThat(ScheduleUtility.calculateRepeatPlusDate(date, ScheduleRepeatType.YEAR, 1))
                .isEqualTo(date.plusYears(1));

        // null date 예외 발생 확인
        assertThatThrownBy(() -> ScheduleUtility.calculateRepeatPlusDate(null, ScheduleRepeatType.DAY, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The date parameters must not be null");

        // 반복 간격이 1 미만일 경우 같은 날짜 반환
        assertThat(ScheduleUtility.calculateRepeatPlusDate(date, ScheduleRepeatType.DAY, 0))
                .isEqualTo(date);
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
                    .hasMessageContaining("잘못된 이메일 형식: " + email);
        }
    }

    @Test
    void hashPasswordTest() {
        String rawPassword = "password123!@#";
        String encodedPassword = "encoded_password123";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(passwordEncoder.matches("password123!@3", encodedPassword)).thenReturn(false);

        UserDto.RegisterRequest userRequest = new UserDto.RegisterRequest(
                "email@naver.com",
                rawPassword,
                "test_king",
                "1234",
                "local"
        );
        UserEntity userEntity = UserEntity.createWithEncodedPassword(userRequest, passwordEncoder);

        assertThat(userEntity.checkPasswordsMatch(rawPassword, passwordEncoder)).isTrue();
        assertThat(userEntity.checkPasswordsMatch("password123!@3", passwordEncoder)).isFalse();
    }

}