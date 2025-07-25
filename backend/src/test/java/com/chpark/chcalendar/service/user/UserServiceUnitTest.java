package com.chpark.chcalendar.service.user;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.dto.security.JwtAuthenticationResponseDto;
import com.chpark.chcalendar.dto.user.UserDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.entity.UserProviderEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.enumClass.RequestType;
import com.chpark.chcalendar.repository.user.UserProviderRepository;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.calendar.CalendarMemberService;
import com.chpark.chcalendar.service.calendar.CalendarService;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import com.chpark.chcalendar.service.redis.RedisService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProviderRepository userProviderRepository;

    @Mock
    private UserCalendarService userCalendarService;

    @Mock
    private CalendarMemberService calendarMemberService;

    @InjectMocks
    private UserService userService;

    @Mock
    private RedisService redisService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Map<CalendarCategory, CalendarService> calendarServiceMap;

    @Mock
    private CalendarService mockCalendarService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void create() {
        //given
        UserDto.RegisterRequest userDto = new UserDto.RegisterRequest("testing1@naver.com", "testpassword123!!", "testingKing", "1234", "local");

        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encoded_password");

        UserEntity savedUser = UserEntity.builder()
                .id(1L)
                .email(userDto.getEmail())
                .password("encoded_password")
                .nickname(userDto.getNickname())
                .build();

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(calendarServiceMap.get(CalendarCategory.USER)).thenReturn(mockCalendarService);
        when(calendarServiceMap.get(CalendarCategory.USER).create(1L, "내 캘린더")).thenReturn(new CalendarDto.Response());

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
                .id(1L)
                .email(userDto.getEmail())
                .password("encoded_password")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(userDto.getEmail()))
                .thenReturn(Optional.of(savedUser));
        when(jwtTokenProvider.generateToken(authentication, savedUser.getId(), JwtTokenType.ACCESS))
                .thenReturn("mocked_jwt_token");

        when(userProviderRepository.findByUserId(savedUser.getId()))
                .thenReturn(List.of(
                        UserProviderEntity.builder()
                                .id(1L)
                                .user(savedUser)
                                .provider("local")
                                .build()
                ));

        // when
        JwtAuthenticationResponseDto token = userService.login(userDto, "1234");

        // then
        assertThat(token.getAccessToken()).isEqualTo("mocked_jwt_token");
    }

    @Test
    void login_UserNotFound() {
        // given
        UserDto userDto = new UserDto("imnotuser@naver.com", "testpassword123!");

        // 사용자 조회 실패하도록 설정
        when(userRepository.findByEmail(userDto.getEmail()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(userDto, "1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다");
    }

    @Test
    void login_InvalidPassword() {
        // given
        UserDto userDto = new UserDto("testing1@naver.com", "wrongpassword");

        UserEntity savedUser = UserEntity.builder()
                .id(1L)
                .email(userDto.getEmail())
                .password("encoded_password")
                .build();

        // 사용자 조회는 성공
        when(userRepository.findByEmail(userDto.getEmail()))
                .thenReturn(Optional.of(savedUser));

        // provider가 local임
        when(userProviderRepository.findByUserId(savedUser.getId()))
                .thenReturn(List.of(
                        UserProviderEntity.builder()
                                .id(1L)
                                .user(savedUser)
                                .provider("local")
                                .build()
                ));

        // 비밀번호가 틀려 인증 실패
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("비밀번호 틀림"));

        // 로그인 요청 횟수 증가와 조회 모킹
        doNothing().when(redisService).increaseRequestCount(RequestType.LOGIN, "1234");
        when(redisService.getRequestCount(RequestType.LOGIN, "1234")).thenReturn("1");

        // when & then
        assertThatThrownBy(() -> userService.login(userDto, "1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일이나 비밀번호를 확인해주세요");
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
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
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
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
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