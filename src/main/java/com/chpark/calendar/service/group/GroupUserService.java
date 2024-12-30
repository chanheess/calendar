package com.chpark.calendar.service.group;

import com.chpark.calendar.dto.group.GroupDto;
import com.chpark.calendar.dto.group.GroupUserDto;
import com.chpark.calendar.entity.group.GroupEntity;
import com.chpark.calendar.entity.group.GroupUserEntity;
import com.chpark.calendar.enumClass.GroupAuthority;
import com.chpark.calendar.repository.group.GroupRepository;
import com.chpark.calendar.repository.group.GroupUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GroupUserService {

    private final GroupUserRepository groupUserRepository;
    private final GroupRepository groupRepository;

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

    public List<GroupDto> findMyGroup(long userId) {
        List<GroupUserEntity> result = groupUserRepository.findByUserId(userId);

        return GroupDto.fromGroupUserEntityList(result);
    }

    public GroupUserDto addUser(long userId, long groupId) {

        GroupEntity entity = groupRepository.findById(groupId).orElseThrow(
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
