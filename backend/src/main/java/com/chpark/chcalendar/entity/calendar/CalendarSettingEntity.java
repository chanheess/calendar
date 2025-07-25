package com.chpark.chcalendar.entity.calendar;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="calendar_setting")
public class CalendarSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "calendar_id", referencedColumnName = "id")
    private CalendarEntity calendar;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 10)
    private String color;

    @Column(nullable = false)
    private Boolean checked = true;

    public CalendarSettingEntity(long userId) {
        this.userId = userId;
        this.color = generateRandomColor();
    }

    private String generateRandomColor() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return String.format("#%02x%02x%02x", r, g, b);
    }


}
