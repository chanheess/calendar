package com.chpark.calendar.entity.schedule;

import com.chpark.calendar.dto.schedule.ScheduleDto;
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
    private Long userId;

    @Column(name = "group_id")
    private Long groupId;

    public ScheduleEntity(ScheduleDto scheduleDto) {
        setTitle(scheduleDto.getTitle());
        setDescription(scheduleDto.getDescription());
        setStartAt(scheduleDto.getStartAt());
        setEndAt(scheduleDto.getEndAt());
    }

}