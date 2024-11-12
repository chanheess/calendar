package com.chpark.calendar.utility;

import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.entity.UserEntity;
import com.chpark.calendar.enumClass.ScheduleRepeatType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

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
        assertThat(ScheduleUtility.calculateRepeatCount(startDate, endDate, 1, ScheduleRepeatType.d))
                .isEqualTo(30);

        // 주간 반복
        assertThat(ScheduleUtility.calculateRepeatCount(startDate, endDate, 1, ScheduleRepeatType.w))
                .isEqualTo(4);

        // 월간 반복
        assertThat(ScheduleUtility.calculateRepeatCount(startDate, endDate, 1, ScheduleRepeatType.m))
                .isEqualTo(0); // (31일 사이에 한 번이니까 반복은 없으므로 0)

        // 연간 반복
        assertThat(ScheduleUtility.calculateRepeatCount(startDate, endDate, 1, ScheduleRepeatType.y))
                .isEqualTo(0); // (같은 해 내 반복이 없으므로 0)

        // 종료일이 없을 때
        assertThat(ScheduleUtility.calculateRepeatCount(startDate, null, 1, ScheduleRepeatType.d))
                .isEqualTo(500);
    }

    @Test
    void calculateRepeatPlusDateTest() {
        LocalDateTime date = LocalDateTime.of(2023, 1, 1, 0, 0);

        // 일간 반복 추가 (interval = 10, repeatType = DAYS)
        assertThat(ScheduleUtility.calculateRepeatPlusDate(date, ScheduleRepeatType.d, 10))
                .isEqualTo(date.plusDays(10));

        // 주간 반복 추가 (interval = 2, repeatType = WEEKS)
        assertThat(ScheduleUtility.calculateRepeatPlusDate(date, ScheduleRepeatType.w, 2))
                .isEqualTo(date.plusWeeks(2));

        // 월간 반복 추가 (interval = 3, repeatType = MONTHS)
        assertThat(ScheduleUtility.calculateRepeatPlusDate(date, ScheduleRepeatType.m, 3))
                .isEqualTo(date.plusMonths(3));

        // 연간 반복 추가 (interval = 1, repeatType = YEARS)
        assertThat(ScheduleUtility.calculateRepeatPlusDate(date, ScheduleRepeatType.y, 1))
                .isEqualTo(date.plusYears(1));

        // null date 예외 발생 확인
        assertThatThrownBy(() -> ScheduleUtility.calculateRepeatPlusDate(null, ScheduleRepeatType.d, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The date parameters must not be null");

        // 반복 간격이 1 미만일 경우 같은 날짜 반환
        assertThat(ScheduleUtility.calculateRepeatPlusDate(date, ScheduleRepeatType.d, 0))
                .isEqualTo(date);
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
                "test_king"
        );
        UserEntity userEntity = UserEntity.createWithEncodedPassword(userRequest, passwordEncoder);

        assertThat(userEntity.checkPassword(rawPassword, passwordEncoder)).isTrue();
        assertThat(userEntity.checkPassword("password123!@3", passwordEncoder)).isFalse();
    }

}