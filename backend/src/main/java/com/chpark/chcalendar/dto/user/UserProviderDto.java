package com.chpark.chcalendar.dto.user;


import com.chpark.chcalendar.entity.UserProviderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProviderDto {
    String provider;

    public UserProviderDto(UserProviderEntity userProviderEntity) {
        this.provider = userProviderEntity.getProvider();
    }

    public static List<UserProviderDto> fromUserProviderEntityList(List<UserProviderEntity> entityList) {
        return entityList.stream()
                .map(UserProviderDto::new)
                .collect(Collectors.toList());
    }
}
