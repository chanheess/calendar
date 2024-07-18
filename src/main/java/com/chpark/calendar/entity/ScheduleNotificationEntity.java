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

    @Column(name = "schedule_id", nullable = false)
    private int scheduleId;

    @Column(name = "notification_at", nullable = false)
    private LocalDateTime notificationAt;

    public ScheduleNotificationEntity(int scheduleId, ScheduleNotificationDto.Request notificationDto) {
        this.scheduleId = scheduleId;
        this.notificationAt = notificationDto.getNotificationAt();
    }
}
