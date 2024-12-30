package com.chpark.calendar.dto.group;

import com.chpark.calendar.entity.group.GroupEntity;
import com.chpark.calendar.entity.group.GroupUserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {

    private Long groupId;
    private String title;

    public GroupDto(GroupEntity groupEntity) {
        this.groupId = groupEntity.getId();
        this.title = groupEntity.getTitle();
    }

    public GroupDto(GroupUserEntity groupUserEntity) {
        this.groupId = groupUserEntity.getId();
        this.title = groupUserEntity.getGroupTitle();
    }

    public static List<GroupDto> fromGroupEntityList(List<GroupEntity> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(GroupDto::new)
                .collect(Collectors.toList());
    }

    public static List<GroupDto> fromGroupUserEntityList(List<GroupUserEntity> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(GroupDto::new)
                .collect(Collectors.toList());
    }
}
