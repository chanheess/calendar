package com.chpark.chcalendar.dto.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.exception.ValidGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleDto {

    private long id;

    @NotNull(groups = ValidGroup.CreateGroup.class)
    private String title;

    private String description;

    @NotNull(groups = ValidGroup.CreateGroup.class)
    private LocalDateTime startAt;

    @NotNull(groups = ValidGroup.CreateGroup.class)
    private LocalDateTime endAt;

    private Long repeatId;

    @NotNull
    private long userId;    //create에서는 필요하지 않음

    @NotNull(groups = ValidGroup.CreateGroup.class)
    private Long calendarId;

    public ScheduleDto(String title, String description, LocalDateTime startAt, LocalDateTime endAt, Long calendarId) {
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.calendarId = calendarId;
    }

    public ScheduleDto(ScheduleEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.startAt = entity.getStartAt();
        this.endAt = entity.getEndAt();
        this.repeatId = entity.getRepeatId();
        this.userId = entity.getUserId();
        this.calendarId = entity.getCalendarId();
    }

    public static ScheduleDto fromScheduleEntity(ScheduleEntity scheduleEntity) {
        return new ScheduleDto(scheduleEntity);
    }

    public static List<ScheduleDto> fromScheduleEntityList(List<ScheduleEntity> entityList) {
        return entityList.stream()
                .map(ScheduleDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ScheduleDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                '}';
    }



    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @Valid
        @NotNull(groups = ValidGroup.CreateGroup.class)
        private ScheduleDto scheduleDto;
        private Set<ScheduleNotificationDto> notificationDto = new HashSet<>();
        private ScheduleRepeatDto repeatDto;
        private Set<ScheduleGroupDto> groupDto = new HashSet<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response {
        private ScheduleDto scheduleDto;
        private List<ScheduleNotificationDto> notificationDto;
        private ScheduleRepeatDto repeatDto;
        private List<ScheduleGroupDto> groupDto;

        public Response(ScheduleDto scheduleDto, List<ScheduleNotificationDto> notificationDto, ScheduleRepeatDto repeatDto, List<ScheduleGroupDto> groupDto) {
            this.scheduleDto = scheduleDto;
            this.notificationDto = notificationDto;
            this.repeatDto = repeatDto;
            this.groupDto = groupDto;
        }
    }

}
