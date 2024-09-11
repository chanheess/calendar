package com.chpark.calendar.service;

import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.entity.UserEntity;
import com.chpark.calendar.repository.user.UserRepository;
import com.chpark.calendar.security.JwtTokenProvider;
import com.chpark.calendar.utility.ScheduleUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestUser.getEmail(), requestUser.getPassword())
        );
        return jwtTokenProvider.generateToken(authentication);
    }
}
