package com.chpark.chcalendar.dto.calendar;

import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.GroupUserEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDto {

    @Size(min = 1, max = 20)
    private String title;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request extends CalendarDto {

        @NotNull
        private CalendarCategory category;
    }

    @Getter
    @NoArgsConstructor
    public static class Response extends CalendarDto {

        private long id;
        private String color;
        private CalendarCategory category;

        @Builder
        public Response(@Size(min = 1, max = 20) String title, long id, String color, CalendarCategory category) {
            super(title);
            this.id = id;
            this.color = color;
            this.category = category;
        }

        public Response(CalendarEntity entity) {
            super(entity.getTitle());
            this.id = entity.getId();
            this.color = entity.getCalendarSetting().getColor();
            this.category = entity.getCategory();
        }

        public Response(GroupUserEntity entity) {
            super(entity.getGroupTitle());
            this.id = entity.getGroupId();
            this.color = entity.getColor();
            this.category = CalendarCategory.GROUP;
        }

        public static List<CalendarDto.Response> fromCalendarEntityList(List<CalendarEntity> entityList) {
            if (entityList == null) {
                return Collections.emptyList();
            }
            return entityList.stream()
                    .map(CalendarDto.Response::new)
                    .collect(Collectors.toList());
        }

        public static List<CalendarDto.Response> fromGroupUserEntityList(List<GroupUserEntity> entityList) {
            if (entityList == null) {
                return Collections.emptyList();
            }
            return entityList.stream()
                    .map(CalendarDto.Response::new)
                    .collect(Collectors.toList());
        }
    }


}
