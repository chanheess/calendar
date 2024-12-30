package com.chpark.calendar.entity.group;

import com.chpark.calendar.enumClass.GroupAuthority;
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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private GroupAuthority role;

    public GroupUserEntity(String groupTitle, Long groupId, Long userId, GroupAuthority role) {
        this.groupTitle = groupTitle;
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
    }
}
