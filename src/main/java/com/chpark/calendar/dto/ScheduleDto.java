package com.chpark.calendar.dto;

import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.entity.ScheduleRepeatEntity;
import com.chpark.calendar.enumClass.ScheduleRepeatType;
import jakarta.validation.constraints.FutureOrPresent;
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

    private int id;

    @NotNull
    private String title;

    private String description;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private Integer repeatId;

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
                ", repeatId=" + repeatId +
                '}';
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        //비교하기 쉽게 상속하지 않고 오브젝트로
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
        private List<ScheduleNotificationDto.Response> notificationDto;
        private ScheduleRepeatDto.Response repeatDto;

        public Response(ScheduleDto scheduleDto) {
            this.scheduleDto = scheduleDto;
            this.notificationDto = null;
            this.repeatDto = null;
        }

        public Response(ScheduleDto scheduleDto, List<ScheduleNotificationDto.Response> notificationDto, ScheduleRepeatDto.Response repeatDto) {
            this.scheduleDto = scheduleDto;
            this.notificationDto = notificationDto;
            this.repeatDto = repeatDto;
        }
    }

}
