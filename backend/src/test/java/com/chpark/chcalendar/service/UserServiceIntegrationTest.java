package com.chpark.chcalendar.service;

import com.chpark.chcalendar.DotenvInitializer;
import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.service.user.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ContextConfiguration(initializers = DotenvInitializer.class)
public class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EntityManager entityManager;

    UserEntity savedUser;

    @BeforeEach
    @Transactional
    void setupUser() {
        //given
        UserDto.RegisterRequest userDto = new UserDto.RegisterRequest("testing1@naver.com",
                "testpassword123!", "testingKing", "1234", "local");

        savedUser = UserEntity.createWithEncodedPassword(userDto, passwordEncoder);
        userRepository.save(savedUser);

        assertThat(savedUser.getEmail()).isEqualTo("testing1@naver.com");
    }

    @Test
    @Transactional
    void updatePassword() {
        //given
        UserDto.ChangePassword password = new UserDto.ChangePassword("testpassword123!", "newGoodPassword2!!");

        //when
        userService.updatePassword(savedUser.getId(), password);

        entityManager.flush();
        entityManager.clear();

        //then
        assertThat(savedUser.checkPasswordsMatch(password.getNewPassword(), passwordEncoder)).isTrue();
    }

    @Test
    @Transactional
    void updatePassword_InvalidPassword() {
        //given
        UserDto.ChangePassword password = new UserDto.ChangePassword("wrongPassword1!", "newGoodPassword2!!");

        //when & then
        assertThatThrownBy(() -> userService.updatePassword(savedUser.getId(), password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("현재 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @Transactional
    void updatePassword_SamePassword() {
        //given
        UserDto.ChangePassword password = new UserDto.ChangePassword("testpassword123!", "testpassword123!");

        //when & then
        assertThatThrownBy(() -> userService.updatePassword(savedUser.getId(), password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("새 비밀번호는 현재 비밀번호와 같을 수 없습니다.");
    }

    @Test
    @Transactional
    void updatePassword_InvalidUserId() {
        //given
        int nonExistentUserId = Integer.MAX_VALUE;
        UserDto.ChangePassword password = new UserDto.ChangePassword("testpassword123", "newGoodPassword!!");

        //when & then
        assertThatThrownBy(() -> userService.updatePassword(nonExistentUserId, password))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }
}
