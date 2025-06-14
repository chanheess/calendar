package com.chpark.chcalendar.entity.calendar;

import com.chpark.chcalendar.enumClass.CalendarCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="calendar")
public class CalendarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalendarCategory category;

    @OneToOne(mappedBy = "calendar")
    private CalendarSettingEntity calendarSetting;

    public void setCalendarSetting(CalendarSettingEntity setting) {
        this.calendarSetting = setting;
        if (setting != null && setting.getCalendar() != this) {
            setting.setCalendar(this);
        }
    }

    public CalendarEntity(String title, long userId, CalendarCategory category) {
        this.title = title;
        this.userId = userId;
        this.category = category;

        setCalendarSetting(new CalendarSettingEntity(this.getUserId()));
    }

    public CalendarEntity(String title, long userId, CalendarCategory category, CalendarSettingEntity calendarSetting) {
        this.title = title;
        this.userId = userId;
        this.category = category;
        this.setCalendarSetting(calendarSetting);
    }
}
