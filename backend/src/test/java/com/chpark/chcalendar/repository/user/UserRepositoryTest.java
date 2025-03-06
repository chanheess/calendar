package com.chpark.chcalendar.repository.user;

import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.entity.UserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    UserEntity savedUser;

    @BeforeEach
    @Transactional
    void setupUser() {
        UserDto.RegisterRequest userDto = new UserDto.RegisterRequest("testing1@naver.com",
                "testpassword123", "testingKing", "1234");

        savedUser = UserEntity.createWithEncodedPassword(userDto, passwordEncoder);
        userRepository.save(savedUser);
    }

    @Test
    @Transactional
    void findByEmail() {
        Optional<UserEntity> userEntity = userRepository.findByEmail(savedUser.getEmail());
        assertThat(userEntity.isPresent()).isTrue();
    }

    @Test
    @Transactional
    void findNicknameById() {
        Optional<UserEntity> userEntity = userRepository.findById(savedUser.getId());
        assertThat(userEntity.isPresent()).isTrue();
    }

    @Test
    @Transactional
    void existsByEmail() {
        assertThat(userRepository.existsByEmail(savedUser.getEmail())).isTrue();
    }

    @Test
    @Transactional
    void existsByNickname() {
        assertThat(userRepository.existsByNickname(savedUser.getNickname())).isTrue();
    }

    @Test
    @Transactional
    void updateUserInfo() {
        UserEntity updateUserInfo = UserEntity.builder()
                .nickname("ImTestKing")
                .build();

        int updatedCount = userRepository.updateUserInfo(savedUser.getId(), updateUserInfo);
        assertThat(updatedCount).isEqualTo(1);

        entityManager.clear();

        Optional<UserEntity> userEntity = userRepository.findById(savedUser.getId());

        assertThat(userEntity.isPresent()).isTrue();
        assertThat(userEntity.get().getNickname()).isEqualTo(updateUserInfo.getNickname());
    }

}