package com.chpark.chcalendar.entity.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import jakarta.persistence.*;
import lombok.Builder;
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

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "etag")
    private String etag;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    public ScheduleEntity(ScheduleDto scheduleDto) {
        this.title = scheduleDto.getTitle();
        this.description = scheduleDto.getDescription();
        this.startAt = scheduleDto.getStartAt();
        this.endAt = scheduleDto.getEndAt();
        this.userId = scheduleDto.getUserId();
        this.calendarId = scheduleDto.getCalendarId();
    }

    @Builder
    public ScheduleEntity(String title, String description, LocalDateTime startAt, LocalDateTime endAt, Long repeatId, long userId, long calendarId, String providerId, String etag, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.repeatId = repeatId;
        this.userId = userId;
        this.calendarId = calendarId;
        this.providerId = providerId;
        this.etag = etag;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void overwrite(ScheduleEntity targetEntity) {
        this.title = targetEntity.getTitle();
        this.description = targetEntity.getDescription();
        this.startAt = targetEntity.getStartAt();
        this.endAt = targetEntity.getEndAt();
        this.repeatId = targetEntity.getRepeatId();
        this.userId = targetEntity.getUserId();
        this.calendarId = targetEntity.getCalendarId();
        this.providerId = targetEntity.getProviderId();
        this.etag = targetEntity.getEtag();
        this.updatedAt = targetEntity.getUpdatedAt();
    }
}