package com.chpark.chcalendar.service;

import com.chpark.chcalendar.DotenvInitializer;
import com.chpark.chcalendar.dto.user.UserDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.CalendarMemberRole;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.service.calendar.CalendarMemberService;
import com.chpark.chcalendar.service.calendar.GroupCalendarService;
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
    private CalendarRepository calendarRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupCalendarService groupCalendarService;

    @Autowired
    private CalendarMemberService calendarMemberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    UserEntity savedUser;

    @BeforeEach
    @Transactional
    void setupUser() {
        //given
        String email = "testing1@naver.com";
        String password = "testpassword123!";
        String nickname = "testingKing";
        String emailCode = "1234";
        String provider = "local";

        savedUser = createUser(email, password, nickname, emailCode, provider);
    }

    @Transactional
    UserEntity createUser(String email, String password, String nickname, String emailCode, String provider) {
        UserDto.RegisterRequest userDto = new UserDto.RegisterRequest(email,
                password, nickname, emailCode, provider);

        UserEntity result = UserEntity.createWithEncodedPassword(userDto, passwordEncoder);
        userRepository.save(result);

        assertThat(result.getEmail()).isEqualTo(email);

        return result;
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

    @Test
    @Transactional
    void deleteAccount() {
        // 유저 추가
        String email = "testing2@naver.com";
        String password = "testpassword123!";
        String nickname = "testingKing2";
        String emailCode = "1234";
        String provider = "local";

        UserEntity userEntity = createUser(email, password, nickname, emailCode, provider);

        // 그룹 캘린더 추가
        CalendarEntity calendarEntity2 = CalendarEntity.builder()
                .title("testGroup")
                .userId(userEntity.getId())
                .category(CalendarCategory.GROUP)
                .build();

        calendarRepository.save(calendarEntity2);
        calendarMemberService.create(calendarEntity2, calendarEntity2.getUserId(), CalendarMemberRole.ADMIN);
        calendarMemberService.addUser(savedUser.getId(), calendarEntity2.getId());

        // 회원 탈퇴
        userService.deleteAccount(savedUser.getId());

        // 검증
        assertThatThrownBy(() -> userService.findUserInfo(savedUser.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }
}
