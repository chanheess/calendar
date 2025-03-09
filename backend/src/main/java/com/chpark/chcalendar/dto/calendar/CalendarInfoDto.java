package com.chpark.chcalendar.dto.calendar;

import com.chpark.chcalendar.entity.CalendarInfoEntity;
import com.chpark.chcalendar.entity.GroupUserEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarInfoDto {

    @Size(min = 1, max = 20)
    private String title;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request extends CalendarInfoDto {

        @NotNull
        private CalendarCategory category;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response extends CalendarInfoDto {

        private long id;
        private String color;
        private CalendarCategory category;

        public Response(CalendarInfoEntity entity) {
            super(entity.getTitle());
            this.id = entity.getId();
            this.color = entity.getColor();
            this.category = entity.getCategory();
        }

        public Response(GroupUserEntity entity) {
            super(entity.getGroupTitle());
            this.id = entity.getGroupId();
            this.color = entity.getColor();
            this.category = CalendarCategory.GROUP;
        }

        public static List<CalendarInfoDto.Response> fromCalendarEntityList(List<CalendarInfoEntity> entityList) {
            if (entityList == null) {
                return Collections.emptyList();
            }
            return entityList.stream()
                    .map(CalendarInfoDto.Response::new)
                    .collect(Collectors.toList());
        }

        public static List<CalendarInfoDto.Response> fromGroupUserEntityList(List<GroupUserEntity> entityList) {
            if (entityList == null) {
                return Collections.emptyList();
            }
            return entityList.stream()
                    .map(CalendarInfoDto.Response::new)
                    .collect(Collectors.toList());
        }
    }


}
