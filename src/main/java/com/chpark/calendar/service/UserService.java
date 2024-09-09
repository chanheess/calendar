package com.chpark.calendar.service;

import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.entity.UserEntity;
import com.chpark.calendar.repository.user.UserRepository;
import com.chpark.calendar.utility.ScheduleUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void createUser(UserDto.PostRequest requestUser) {
        if(userRepository.existsByEmail(requestUser.getEmail())) {
            throw new IllegalArgumentException("이미 해당 \"이메일\"을 가진 사용자가 존재합니다.");
        }
        if(userRepository.existsByNickname(requestUser.getNickname())) {
            throw new IllegalArgumentException("이미 해당 \"닉네임\"을 가진 사용자가 존재합니다.");
        }

        ScheduleUtility.validateEmail(requestUser.getEmail());

        userRepository.save(new UserEntity(requestUser, passwordEncoder));
    }

}
