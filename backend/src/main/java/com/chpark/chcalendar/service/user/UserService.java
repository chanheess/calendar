package com.chpark.chcalendar.service.user;

import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.enumClass.RequestType;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.redis.RedisService;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import com.chpark.chcalendar.utility.ScheduleUtility;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserCalendarService userCalendarService;
    private final PasswordEncoder passwordEncoder;
    private final GroupUserService groupUserService;
    private final RedisService redisService;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void create(UserDto.RegisterRequest requestUser) {
        if (userRepository.existsByEmail(requestUser.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        if (userRepository.existsByNickname(requestUser.getNickname())) {
            throw new IllegalArgumentException("An account with this nickname already exists.");
        }

        ScheduleUtility.validateEmail(requestUser.getEmail());
        UserEntity.validatePassword(requestUser.getPassword());

        UserEntity user = userRepository.save(UserEntity.createWithEncodedPassword(requestUser, passwordEncoder));

        //기본 캘린더 생성
        userCalendarService.create(user.getId(), "내 캘린더");
    }

    @Transactional
    public String login(UserDto requestUser,String ipAddress) {

        redisService.checkRequestCount(RequestType.LOGIN, ipAddress);
        redisService.increaseRequestCount(RequestType.LOGIN, ipAddress);

        try {
            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestUser.getEmail(), requestUser.getPassword())
            );

            // 인증 성공 시 사용자 정보를 가져옴
            UserEntity userEntity = userRepository.findByEmail(requestUser.getEmail()).orElseThrow(
                    () -> new EntityNotFoundException("User not found with email: " + requestUser.getEmail())
            );

            redisService.deleteVerificationData(RequestType.LOGIN, ipAddress);

            // JWT 토큰 생성 후 반환
            return jwtTokenProvider.generateToken(authentication, userEntity.getId());
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid email or password. (%s)", redisService.getRequestCount(RequestType.LOGIN, ipAddress))
            );
        }
    }

    public String findNickname(long userId) {
        return userRepository.findNicknameById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found"));
    }

    public long findUserId(String nickname) {
        return userRepository.findIdByNickname(nickname).orElseThrow(
                () -> new EntityNotFoundException("User does not exist.")
        );
    }

    public UserDto.UserInfo findUserInfo(long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found"));

        return new UserDto.UserInfo(userEntity);
    }

    @Transactional
    public void updateUserInfo(long userId, UserDto.UserInfo userInfo) {
        if (userRepository.existsByEmail(userInfo.getEmail())) {
            throw new IllegalArgumentException("이미 해당 이메일을 가진 사용자가 존재합니다.");
        }
        if (userRepository.existsByNickname(userInfo.getNickname())) {
            throw new IllegalArgumentException("이미 해당 닉네임을 가진 사용자가 존재합니다.");
        }

        UserEntity userEntity = UserEntity.builder()
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .build();
        userRepository.updateUserInfo(userId, userEntity);

        //닉네임 수정시 동기화
        groupUserService.updateGroupUserNickname(userId, userEntity.getNickname());
    }

    @Transactional
    public void updatePassword(long userId, UserDto.ChangePassword password) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found"));

        UserEntity.validatePassword(password.getNewPassword());

        if (!userEntity.checkPasswordsMatch(password.getCurrentPassword(), passwordEncoder)) {
            throw new IllegalArgumentException("Incorrect password");
        }

        if (userEntity.checkPasswordsMatch(password.getNewPassword(), passwordEncoder)) {
            throw new IllegalArgumentException("New password can't be the same as the current password.");
        }

        userEntity.changePassword(password.getNewPassword(), passwordEncoder);
    }

    @Transactional
    public void resetPassword(UserDto.ResetPassword userDto, String ipAddress) {
        UserEntity userEntity = userRepository.findByEmail(userDto.getEmail()).orElseThrow(
            () -> new EntityNotFoundException("User not found"));

        UserEntity.validatePassword(userDto.getPassword());
        userEntity.changePassword(userDto.getPassword(), passwordEncoder);

        redisService.deleteVerificationData(RequestType.LOGIN, ipAddress);
    }
}
