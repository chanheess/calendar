package com.chpark.chcalendar.service.user;


import com.chpark.chcalendar.dto.EmailDto;
import com.chpark.chcalendar.dto.security.JwtAuthenticationResponseDto;
import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.enumClass.RequestType;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import com.chpark.chcalendar.service.redis.RedisService;
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
        validateCreateUser(requestUser);

        UserEntity user = userRepository.save(UserEntity.createWithEncodedPassword(requestUser, passwordEncoder));

        //기본 캘린더 생성
        userCalendarService.create(user.getId(), "내 캘린더");
    }

    @Transactional
    public void validateCreateUser(UserDto.RegisterRequest requestUser) {
        if (userRepository.existsByEmail(requestUser.getEmail())) {
            throw new IllegalArgumentException("이미 해당 이메일을 가진 사용자가 존재합니다.");
        }
        if (userRepository.existsByNickname(requestUser.getNickname())) {
            throw new IllegalArgumentException("이미 해당 닉네임을 가진 사용자가 존재합니다.");
        }

        ScheduleUtility.validateEmail(requestUser.getEmail());
        UserEntity.validatePassword(requestUser.getPassword());

        //이메일 검증이 완료되면 요청 카운트 삭제되기에 이메일 검증 이후에 검증이 없어야한다.
        EmailDto emailDto = new EmailDto(requestUser.getEmail(), RequestType.REGISTER);
        redisService.verificationEmail(emailDto, requestUser.getEmailCode());
    }

    @Transactional
    public JwtAuthenticationResponseDto login(UserDto requestUser, String ipAddress) {

        redisService.checkRequestCount(RequestType.LOGIN, ipAddress);
        redisService.increaseRequestCount(RequestType.LOGIN, ipAddress);

        try {
            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestUser.getEmail(), requestUser.getPassword())
            );

            // 인증 성공 시 사용자 정보를 가져옴
            UserEntity userEntity = userRepository.findByEmail(requestUser.getEmail()).orElseThrow(
                    () -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + requestUser.getEmail())
            );

            redisService.deleteVerificationData(RequestType.LOGIN, ipAddress);

            // JWT 토큰 생성 후 반환
            return new JwtAuthenticationResponseDto(
                    jwtTokenProvider.generateAccessToken(authentication, userEntity.getId()),
                    jwtTokenProvider.generateRefreshToken(authentication, userEntity.getId()),
                    "로그인 성공."
            );
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException(String.format(
                    "이메일이나 비밀번호를 확인해주세요. (%s)", redisService.getRequestCount(RequestType.LOGIN, ipAddress))
            );
        }
    }

    public String findNickname(long userId) {
        return userRepository.findNicknameById(userId).orElseThrow(
                () -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    public long findUserId(String nickname) {
        return userRepository.findIdByNickname(nickname).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 사용자입니다."));
    }

    public UserDto.UserInfo findUserInfo(long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

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
                () -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        UserEntity.validatePassword(password.getNewPassword());

        if (!userEntity.checkPasswordsMatch(password.getCurrentPassword(), passwordEncoder)) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (userEntity.checkPasswordsMatch(password.getNewPassword(), passwordEncoder)) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 같을 수 없습니다.");
        }

        userEntity.changePassword(password.getNewPassword(), passwordEncoder);
    }

    @Transactional
    public void resetPassword(UserDto.ResetPassword userDto, String ipAddress) {
        UserEntity userEntity = userRepository.findByEmail(userDto.getEmail()).orElseThrow(
            () -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        UserEntity.validatePassword(userDto.getPassword());
        userEntity.changePassword(userDto.getPassword(), passwordEncoder);

        redisService.deleteVerificationData(RequestType.LOGIN, ipAddress);
    }
}
