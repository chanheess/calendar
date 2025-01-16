package com.chpark.chcalendar.entity.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="schedule")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "repeat_id")
    private Long repeatId;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "calendar_id")
    private long calendarId;

    public ScheduleEntity(ScheduleDto scheduleDto) {

        this.title = scheduleDto.getTitle();
        this.description = scheduleDto.getDescription();
        this.startAt = scheduleDto.getStartAt();
        this.endAt = scheduleDto.getEndAt();
        this.userId = scheduleDto.getUserId();
        this.calendarId = scheduleDto.getCalendarId();
    }

}