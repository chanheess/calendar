package com.chpark.calendar.dto.schedule;

import com.chpark.calendar.entity.schedule.ScheduleEntity;
import com.chpark.calendar.exception.ValidGroup;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    public ScheduleDto(String title, String description, LocalDateTime startAt, LocalDateTime endAt) {
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public ScheduleDto(ScheduleEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.startAt = entity.getStartAt();
        this.endAt = entity.getEndAt();
        this.repeatId = entity.getRepeatId();
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
        //비교하기 쉽게 상속하지 않고 오브젝트로
        @NotNull(groups = ValidGroup.CreateGroup.class)
        private ScheduleDto scheduleDto;

        private List<ScheduleNotificationDto> notificationDto = new ArrayList<>();
        private ScheduleRepeatDto repeatDto;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response {
        //비교하기 쉽게 상속하지 않고 오브젝트로
        private ScheduleDto scheduleDto;
        private List<ScheduleNotificationDto> notificationDto;
        private ScheduleRepeatDto repeatDto;

        public Response(ScheduleDto scheduleDto, List<ScheduleNotificationDto> notificationDto) {
            this.scheduleDto = scheduleDto;
            this.notificationDto = notificationDto;
            this.repeatDto = null;
        }

        public Response(ScheduleDto scheduleDto, List<ScheduleNotificationDto> notificationDto, ScheduleRepeatDto repeatDto) {
            this.scheduleDto = scheduleDto;
            this.notificationDto = notificationDto;
            this.repeatDto = repeatDto;
        }
    }

}
