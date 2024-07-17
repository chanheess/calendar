package com.chpark.calendar.entity;

import com.chpark.calendar.dto.ScheduleNotificationDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="schedule_notification")
public class ScheduleNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "notification_at", nullable = false)
    private LocalDateTime notificationAt;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;

    public ScheduleNotificationEntity(ScheduleEntity requestSchedule, ScheduleNotificationDto.Request notificationDto) {
        setSchedule(requestSchedule);
        setNotificationAt(notificationDto.getNotificationAt());
    }
}
