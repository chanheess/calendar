package com.chpark.calendar.service.group;

import com.chpark.calendar.dto.group.GroupDto;
import com.chpark.calendar.dto.group.GroupUserDto;
import com.chpark.calendar.entity.group.GroupEntity;
import com.chpark.calendar.enumClass.GroupAuthority;
import com.chpark.calendar.repository.group.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GroupService {

    private final GroupUserService groupUserService;
    private final GroupRepository groupRepository;

    public GroupDto create(long userId, String title) {

        int maxAdminCount = 10;

        if (maxAdminCount <= groupRepository.countAdminGroups(userId)) {
            throw new IllegalArgumentException("You have reached the maximum limit for creating groups.");
        }

        GroupEntity groupEntity = new GroupEntity(title, userId);

        groupRepository.save(groupEntity);
        groupUserService.create(new GroupUserDto(
                groupEntity.getTitle(),
                groupEntity.getId(),
                groupEntity.getAdminId(),
                GroupAuthority.ADMIN)
        );

        return new GroupDto(groupEntity.getId(), groupEntity.getTitle());
    }

    public List<GroupDto> findGroup(String title) {
        List<GroupEntity> groupEntity = groupRepository.findByTitle(title);

        return GroupDto.fromGroupEntityList(groupEntity);
    }
}
