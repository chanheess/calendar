package com.chpark.chcalendar.dto.group;

import com.chpark.chcalendar.entity.GroupUserEntity;
import com.chpark.chcalendar.enumClass.GroupAuthority;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GroupUserDto {

    @NotNull
    private String groupTitle;
    @NotNull
    private Long groupId;
    @NotNull
    private String userNickname;
    @NotNull
    private Long userId;
    @NotNull
    private GroupAuthority role;
    @NotNull
    private String color;

    public GroupUserDto(GroupUserEntity groupUserEntity) {
        this.groupTitle = groupUserEntity.getGroupTitle();
        this.groupId = groupUserEntity.getGroupId();
        this.userNickname = groupUserEntity.getUserNickname();
        this.userId = groupUserEntity.getUserId();
        this.role = groupUserEntity.getRole();
        this.color = groupUserEntity.getColor();
    }

    public static List<GroupUserDto> fromGroupUserEntityList(List<GroupUserEntity> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(GroupUserDto::new)
                .collect(Collectors.toList());
    }

}