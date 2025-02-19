package com.chpark.chcalendar.service;

import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import com.chpark.chcalendar.service.group.GroupUserService;
import com.chpark.chcalendar.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCalendarService userCalendarService;

    @Mock
    private GroupUserService groupUserService;

    @InjectMocks
    private UserService userService;

    @Mock
    private RedisService redisService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void create() {
        //given
        UserDto.RegisterRequest userDto = new UserDto.RegisterRequest("testing1@naver.com", "testpassword123!!", "testingKing", "1234");

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(userDto.getNickname())).thenReturn(false);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encoded_password");

        UserEntity savedUser = UserEntity.builder()
                .email(userDto.getEmail())
                .password("encoded_password")
                .nickname(userDto.getNickname())
                .build();

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(userCalendarService.create(0L, "내 캘린더")).thenReturn(new CalendarInfoDto.Response());

        //when
        userService.create(userDto);

        //then
        verify(userRepository).save(any(UserEntity.class)); // save 메서드가 호출되었는지 확인
    }

    @Test
    void login() {
        // given
        UserDto userDto = new UserDto("testing1@naver.com", "testpassword123!!");
        UserEntity savedUser = UserEntity.builder()
                .email(userDto.getEmail())
                .password("encoded_password")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(userDto.getEmail()))
                .thenReturn(Optional.of(savedUser));
        when(jwtTokenProvider.generateToken(authentication, savedUser.getId()))
                .thenReturn("mocked_jwt_token");

        // when
        String token = userService.login(userDto, "1234");

        // then
        assertThat(token).isEqualTo("mocked_jwt_token");
    }

    @Test
    void login_UserNotFound() {
        // given
        UserDto userDto = new UserDto("imnotuser@naver.com", "testpassword123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new IllegalArgumentException("Invalid email or password."));

        // when & then
        assertThatThrownBy(() -> userService.login(userDto, "1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email or password.");
    }

    @Test
    void login_InvalidPassword() {
        // given
        UserDto userDto = new UserDto("testing1@naver.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new IllegalArgumentException("Invalid email or password."));

        // when & then
        assertThatThrownBy(() -> userService.login(userDto, "1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email or password.");
    }

    @Test
    void findUserNickname() {
        //given
        long userId = 1234;
        when(userRepository.findNicknameById(userId)).thenReturn(Optional.of("TestNickname"));

        //when
        String resultNickname = userService.findNickname(userId);

        //then
        assertThat(resultNickname).isEqualTo("TestNickname");
    }

    @Test
    void findUserNickname_userIdNotFound() {
        //given
        long userId = 1234;
        when(userRepository.findNicknameById(userId)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> userService.findNickname(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void findUserInfo() {
        //given
        long userId = 1234;
        UserEntity userEntity = UserEntity.builder()
                .email("testing1@naver.com")
                .nickname("testingKing")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        //when
        UserDto.UserInfo userInfo = userService.findUserInfo(userId);

        //then
        assertThat(userInfo.getEmail()).isEqualTo("testing1@naver.com");
    }

    @Test
    void findUserInfo_userIdNotFound() {
        //given
        long userId = 1234;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> userService.findUserInfo(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUserInfo() {
        //given
        long userId = 1234;
        UserEntity userEntity = UserEntity.builder()
                .email("testing1@naver.com")
                .nickname("testingKing")
                .build();
        UserDto.UserInfo userInfo = new UserDto.UserInfo(userEntity);

        when(userRepository.existsByEmail(userInfo.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(userInfo.getNickname())).thenReturn(false);
        doNothing().when(groupUserService).updateGroupUserNickname(userId, userEntity.getNickname());

        //when
        userService.updateUserInfo(userId, userInfo);

        //then
        verify(userRepository).updateUserInfo(eq(1234L), any(UserEntity.class));
    }

    @Test
    void updateUserInfo_sameNickname() {
        //given
        long userId = 1234;
        UserEntity userEntity = UserEntity.builder()
                .email("testing1@naver.com")
                .nickname("testingKing")
                .build();
        UserDto.UserInfo userInfo = new UserDto.UserInfo(userEntity);

        when(userRepository.existsByEmail(userInfo.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(userInfo.getNickname())).thenReturn(true);

        //when & then
        assertThatThrownBy(() -> userService.updateUserInfo(userId, userInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 해당 닉네임을 가진 사용자가 존재합니다.");
    }
}