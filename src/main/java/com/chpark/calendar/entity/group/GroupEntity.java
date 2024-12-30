package com.chpark.calendar.entity.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="group_info")
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 20)
    private String title;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    public GroupEntity(String title, Long adminId) {
        this.title = title;
        this.adminId = adminId;
    }
}
