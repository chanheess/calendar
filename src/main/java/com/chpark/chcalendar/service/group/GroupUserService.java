package com.chpark.chcalendar.service.group;

import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.dto.group.GroupUserDto;
import com.chpark.chcalendar.entity.CalendarInfoEntity;
import com.chpark.chcalendar.entity.GroupUserEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.GroupAuthority;
import com.chpark.chcalendar.repository.CalendarInfoRepository;
import com.chpark.chcalendar.repository.GroupUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GroupUserService {

    private final GroupUserRepository groupUserRepository;
    private final CalendarInfoRepository calendarInfoRepository;

    public GroupUserDto create(GroupUserDto groupUserDto) {
        GroupUserEntity groupUserEntity = new GroupUserEntity(
                groupUserDto.getGroupTitle(),
                groupUserDto.getGroupId(),
                groupUserDto.getUserId(),
                groupUserDto.getRole()
        );

        groupUserRepository.save(groupUserEntity);

        return new GroupUserDto(
                groupUserEntity.getGroupTitle(),
                groupUserEntity.getGroupId(),
                groupUserEntity.getId(),
                groupUserEntity.getRole()
        );
    }

    public List<CalendarInfoDto.Response> findMyGroup(long userId) {
        List<GroupUserEntity> result = groupUserRepository.findByUserId(userId);

        return CalendarInfoDto.Response.fromGroupUserEntityList(result);
    }

    public List<Long> findMyGroupsId(long userId) {
        return groupUserRepository.findIdByUserId(userId);
    }

    public GroupUserDto addUser(long userId, long groupId) {

        CalendarInfoEntity entity = calendarInfoRepository.findByIdAndCategory(groupId, CalendarCategory.GROUP).orElseThrow(
                () -> new IllegalArgumentException("The group does not exist.")
        );

        GroupUserDto groupUserDto = new GroupUserDto(
                entity.getTitle(),
                groupId,
                userId,
                GroupAuthority.USER
        );

        return this.create(groupUserDto);
    }
}
