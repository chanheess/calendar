package com.chpark.chcalendar.entity.calendar;

import com.chpark.chcalendar.enumClass.CalendarCategory;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="calendar")
public class CalendarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalendarCategory category;

    @OneToMany(mappedBy = "calendar", fetch = FetchType.LAZY)
    private List<CalendarSettingEntity> calendarSettings = new ArrayList<>();

    @OneToOne(mappedBy = "calendar", fetch = FetchType.LAZY)
    private CalendarProviderEntity calendarProvider;

    public void addCalendarSetting(CalendarSettingEntity setting) {
        this.calendarSettings.add(setting);
        setting.setCalendar(this);
    }

    @Builder
    public CalendarEntity(Long id, String title, long userId, CalendarCategory category) {
        this.id = id;
        this.title = title;
        this.userId = userId;
        this.category = category;

        addCalendarSetting(new CalendarSettingEntity(this.getUserId()));
    }

    public CalendarEntity(String title, long userId, CalendarCategory category, CalendarSettingEntity calendarSetting) {
        this.title = title;
        this.userId = userId;
        this.category = category;
        this.addCalendarSetting(calendarSetting);
    }
}
