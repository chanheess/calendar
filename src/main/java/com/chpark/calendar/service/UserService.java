package com.chpark.calendar.service;

import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.entity.UserEntity;
import com.chpark.calendar.repository.user.UserRepository;
import com.chpark.calendar.security.JwtTokenProvider;
import com.chpark.calendar.utility.ScheduleUtility;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void createUser(UserDto.RegisterRequest requestUser) {
        if(userRepository.existsByEmail(requestUser.getEmail())) {
            throw new IllegalArgumentException("이미 해당 \"이메일\"을 가진 사용자가 존재합니다.");
        }
        if(userRepository.existsByNickname(requestUser.getNickname())) {
            throw new IllegalArgumentException("이미 해당 \"닉네임\"을 가진 사용자가 존재합니다.");
        }

        ScheduleUtility.validateEmail(requestUser.getEmail());
        userRepository.save(new UserEntity(requestUser, passwordEncoder));
    }

    @Transactional
    public String loginUser(UserDto requestUser) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestUser.getEmail(), requestUser.getPassword())
            );
            //TODO: id 검색만 하면됨
            UserEntity userEntity = userRepository.findByEmail(requestUser.getEmail()).orElseThrow(
                    () -> new UsernameNotFoundException("User not found with email: " + requestUser.getEmail())
            );

            return jwtTokenProvider.generateToken(authentication, userEntity.getId());
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid credentials provided.");
        }
    }

    @Transactional(readOnly = true)
    public String findUserNickname(int userId) {
        try {
            return userRepository.findNicknameById(userId).orElseThrow(
                    () -> new UsernameNotFoundException("User not found")
            );
        } catch (Exception exception) {
            return exception.getMessage();
        }
    }

    @Transactional(readOnly = true)
    public UserDto.UserInfo findUserInfo(int userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );

        return new UserDto.UserInfo(userEntity);
    }

    @Transactional
    public void updateUserInfo(int userId, UserDto.UserInfo userInfo) {
        if(userRepository.existsByEmail(userInfo.getEmail())) {
            throw new IllegalArgumentException("이미 해당 \"이메일\"을 가진 사용자가 존재합니다.");
        }
        if(userRepository.existsByNickname(userInfo.getNickname())) {
            throw new IllegalArgumentException("이미 해당 \"닉네임\"을 가진 사용자가 존재합니다.");
        }

        UserEntity userEntity = new UserEntity(userInfo);
        userRepository.updateUserInfo(userId, userEntity);
    }
}
