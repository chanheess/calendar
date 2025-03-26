package com.chpark.chcalendar.entity;

import com.chpark.chcalendar.enumClass.CalendarCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Random;

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
    private String color = generateRandomColor();

    @PrePersist
    public void prePersist() {
        if (this.color == null || this.color.isEmpty()) {
            this.color = generateRandomColor();
        }
    }

    private String generateRandomColor() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return String.format("#%02x%02x%02x", r, g, b);
    }

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
