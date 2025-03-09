package com.chpark.chcalendar.entity;

import com.chpark.chcalendar.enumClass.CalendarCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="calendar_info")
public class CalendarInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(name = "admin_id")
    private long adminId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalendarCategory category;

    @Column(nullable = false, length = 20)
    private String color = "#3788d8";

    public CalendarInfoEntity(String title, long adminId, CalendarCategory category) {
        this.title = title;
        this.adminId = adminId;
        this.category = category;
    }

    public CalendarInfoEntity(String color, CalendarCategory category, long adminId, String title) {
        this.color = color;
        this.category = category;
        this.adminId = adminId;
        this.title = title;
    }
}
