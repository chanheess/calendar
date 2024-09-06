package com.chpark.calendar.utility;

import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class ScheduleUtilityTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        // 성공할 이메일 리스트
        List<String> validEmails = new ArrayList<>();
        validEmails.add("test@example.com");
        validEmails.add("user.name@domain.com");

        // 실패할 이메일 리스트
        List<String> invalidEmails = new ArrayList<>();
        invalidEmails.add("invalid-email");
        invalidEmails.add("user@.com");

        // 성공 케이스 테스트
        for (String email : validEmails) {
            assertDoesNotThrow(() -> ScheduleUtility.validateEmail(email), "Should not throw exception for valid email: " + email);
        }

        // 실패 케이스 테스트
        for (String email : invalidEmails) {
            assertThrows(IllegalArgumentException.class, () -> ScheduleUtility.validateEmail(email), "Should throw exception for invalid email: " + email);
        }
    }

    @Test
    void hashPasswordTest() {
        UserDto.PostRequest userRequest = new UserDto.PostRequest(
                "email@naver.com",
                "password123!@#",
                "test_king"
        );
        UserEntity userEntity = new UserEntity(userRequest, passwordEncoder);

        String testPassword = "password123!@#";
        String testPassword2 = "password123!@3";

        log.info(userEntity.toString());
        Assert.isTrue(userEntity.checkPassword(testPassword, passwordEncoder), "문제가 있네");
        Assert.isTrue(!userEntity.checkPassword(testPassword2, passwordEncoder), "문제가 있네");
    }

}