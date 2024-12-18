package com.chpark.calendar.entity;

import com.chpark.calendar.dto.ScheduleNotificationDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="schedule_notification")
public class ScheduleNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "schedule_id", nullable = false)
    private long scheduleId;

    @Column(name = "notification_at", nullable = false)
    private LocalDateTime notificationAt;

    public ScheduleNotificationEntity(long scheduleId, LocalDateTime notificationAt) {
        this.scheduleId = scheduleId;
        this.notificationAt = notificationAt;
    }

    public ScheduleNotificationEntity(long scheduleId, ScheduleNotificationDto notificationDto) {
        this.scheduleId = scheduleId;
        this.notificationAt = notificationDto.getNotificationAt();
    }

    public static List<ScheduleNotificationEntity> fromScheduleNotificationDtoList(long scheduleId, List<ScheduleNotificationDto> dtoList) {
        return dtoList.stream()
                .map(dto -> new ScheduleNotificationEntity(scheduleId, dto))
                .collect(Collectors.toList());
    }
}
