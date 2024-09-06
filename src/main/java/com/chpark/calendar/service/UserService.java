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
        //TODO: email 가지고 있는지 확인
        //TODO: 닉네임 가지고 있는지 확인

        ScheduleUtility.validateEmail(requestUser.getEmailId());

        userRepository.save(new UserEntity(requestUser, passwordEncoder));
    }
}
