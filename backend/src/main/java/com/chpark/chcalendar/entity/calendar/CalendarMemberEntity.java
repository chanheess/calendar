package com.chpark.chcalendar.entity.calendar;

import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.enumClass.CalendarMemberRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="calendar_member")
public class CalendarMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", referencedColumnName = "id")
    private CalendarEntity calendar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalendarMemberRole role = CalendarMemberRole.USER;

    public CalendarMemberEntity(CalendarEntity calendar, UserEntity user, CalendarMemberRole calendarMemberRole) {
        this.calendar = calendar;
        this.user = user;
        this.role = calendarMemberRole;
    }
}