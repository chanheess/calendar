package com.chpark.calendar.dto;

import com.chpark.calendar.entity.CalendarInfoEntity;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class CalendarInfoDto {

    @Size(min = 1, max = 20)
    String title;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response extends CalendarInfoDto {

        long calendarId;

        public Response(CalendarInfoEntity entity) {
            this.title = entity.getTitle();
            this.calendarId = entity.getId();
        }

        public static List<CalendarInfoDto.Response> fromCalendarEntityList(List<CalendarInfoEntity> entityList) {
            if (entityList == null) {
                return Collections.emptyList();
            }
            return entityList.stream()
                    .map(CalendarInfoDto.Response::new)
                    .collect(Collectors.toList());
        }
    }
}
