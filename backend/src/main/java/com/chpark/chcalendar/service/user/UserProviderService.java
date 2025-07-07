package com.chpark.chcalendar.service.user;

import com.chpark.chcalendar.dto.user.UserProviderDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserProviderService {

    private final UserRepository userRepository;

    @Transactional
    public List<UserProviderDto> findUserProvider(long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("등록된 유저가 없습니다.")
        );

        return UserProviderDto.fromUserProviderEntityList(userEntity.getProviders().stream().toList());
    }
}
