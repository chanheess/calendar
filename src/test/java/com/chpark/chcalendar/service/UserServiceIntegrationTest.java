package com.chpark.chcalendar.service;

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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
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
                "testpassword123", "testingKing", "1234");

        savedUser = UserEntity.createWithEncodedPassword(userDto, passwordEncoder);
        userRepository.save(savedUser);

        assertThat(savedUser.getEmail()).isEqualTo("testing1@naver.com");
    }

    @Test
    @Transactional
    void updatePassword() {
        //given
        UserDto.ChangePassword password = new UserDto.ChangePassword("testpassword123", "newGoodPassword!!");

        //when
        userService.updatePassword(savedUser.getId(), password);

        entityManager.flush();
        entityManager.clear();

        //then
        assertThat(savedUser.checkPassword(password.getNewPassword(), passwordEncoder)).isTrue();
    }

    @Test
    @Transactional
    void updatePassword_InvalidPassword() {
        //given
        UserDto.ChangePassword password = new UserDto.ChangePassword("wrongPassword", "newGoodPassword!!");

        //when & then
        assertThatThrownBy(() -> userService.updatePassword(savedUser.getId(), password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Incorrect password");
    }

    @Test
    @Transactional
    void updatePassword_SamePassword() {
        //given
        UserDto.ChangePassword password = new UserDto.ChangePassword("testpassword123", "testpassword123");

        //when & then
        assertThatThrownBy(() -> userService.updatePassword(savedUser.getId(), password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("New password can't be the same as the current password.");
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
                .hasMessageContaining("User not found");
    }
}
