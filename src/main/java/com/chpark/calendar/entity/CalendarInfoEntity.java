package com.chpark.calendar.entity;

import com.chpark.calendar.enumClass.CalendarCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name="calendar_info")
public class CalendarInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(name = "user_id")
    private long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CalendarCategory category;

    public CalendarInfoEntity(String title, long userId, CalendarCategory category) {
        this.title = title;
        this.userId = userId;
        this.category = category;
    }
}
