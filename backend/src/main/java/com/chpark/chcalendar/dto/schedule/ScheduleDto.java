package com.chpark.chcalendar.dto.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.exception.ValidGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    private String providerId;

    private String etag;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ScheduleDto(ScheduleEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.startAt = entity.getStartAt();
        this.endAt = entity.getEndAt();
        this.repeatId = entity.getRepeatId();
        this.userId = entity.getUserId();
        this.calendarId = entity.getCalendarId();
        this.providerId = entity.getProviderId();
        this.etag = entity.getEtag();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    public static List<ScheduleDto> fromScheduleEntityList(List<ScheduleEntity> entityList) {
        return entityList.stream()
                .map(ScheduleDto::new)
                .collect(Collectors.toList());
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
