package com.chpark.chcalendar.entity;

import com.chpark.chcalendar.dto.group.GroupUserDto;
import com.chpark.chcalendar.enumClass.GroupAuthority;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name="group_user")
public class GroupUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_title", nullable = false, length = 20)
    private String groupTitle;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_nickname", nullable = false)
    private String userNickname;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private GroupAuthority role;

    @Column
    private String color = "#3788d8";

    public void setColor(String color) {
        this.color = color;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }



    public GroupUserEntity(String groupTitle, Long groupId, String userNickname, Long userId, GroupAuthority role, String color) {
        this.groupTitle = groupTitle;
        this.groupId = groupId;
        this.userNickname = userNickname;
        this.userId = userId;
        this.role = role;
        this.color = color;
    }

    public GroupUserEntity(GroupUserDto groupUserDto) {
        this.groupTitle = groupUserDto.getGroupTitle();
        this.groupId = groupUserDto.getGroupId();
        this.userNickname = groupUserDto.getUserNickname();
        this.userId = groupUserDto.getUserId();
        this.role = groupUserDto.getRole();
        this.color = groupUserDto.getColor();
    }
}
