package com.chpark.calendar.dto;

import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.entity.ScheduleRepeatEntity;
import com.chpark.calendar.enumClass.ScheduleRepeatType;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleDto {

    private int id;
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public ScheduleDto(ScheduleEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.startAt = entity.getStartAt();
        this.endAt = entity.getEndAt();
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
    public static class repeatRequest {
        //비교하기 쉽게 상속하지 않고 오브젝트로
        private ScheduleDto scheduleDto;
        private ScheduleRepeatDto repeatDto;

        public repeatRequest(ScheduleDto scheduleDto) {
            this.scheduleDto = scheduleDto;
            this.repeatDto = new ScheduleRepeatDto();
        }

        public repeatRequest(ScheduleDto scheduleDto, ScheduleRepeatDto repeatDto) {
            this.scheduleDto = scheduleDto;
            this.repeatDto = repeatDto;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class repeatResponse {
        //비교하기 쉽게 상속하지 않고 오브젝트로
        private ScheduleDto scheduleDto;
        private ScheduleRepeatDto.Response repeatDto;

        public repeatResponse(ScheduleDto scheduleDto) {
            this.scheduleDto = scheduleDto;
            this.repeatDto = new ScheduleRepeatDto.Response();
        }

        public repeatResponse(ScheduleDto scheduleDto, ScheduleRepeatDto.Response repeatDto) {
            this.scheduleDto = scheduleDto;
            this.repeatDto = repeatDto;
        }
    }

}
