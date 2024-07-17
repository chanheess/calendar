package com.chpark.calendar.entity;

import com.chpark.calendar.dto.ScheduleDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="schedule")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ScheduleNotificationEntity> notifications = new ArrayList<>();

    public void addNotification(ScheduleNotificationEntity notification) {
        notifications.add(notification);
        notification.setSchedule(this);
    }

    @Override
    public String toString() {
        return "ScheduleEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                '}';
    }

    public ScheduleEntity(ScheduleDto scheduleDto) {
        setTitle(scheduleDto.getTitle());
        setDescription(scheduleDto.getDescription());
        setStartAt(scheduleDto.getStartAt());
        setEndAt(scheduleDto.getEndAt());
    }

}
