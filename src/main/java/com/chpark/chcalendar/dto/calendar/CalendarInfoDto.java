package com.chpark.chcalendar.dto.calendar;

import com.chpark.chcalendar.entity.CalendarInfoEntity;
import com.chpark.chcalendar.entity.GroupUserEntity;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CalendarInfoDto {

    long id;
    
    @Size(min = 1, max = 20)
    String title;

    public CalendarInfoDto(CalendarInfoEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
    }

    public CalendarInfoDto(GroupUserEntity entity) {
        this.id = entity.getId();
        this.title = entity.getGroupTitle();
    }

    public static List<CalendarInfoDto> fromCalendarEntityList(List<CalendarInfoEntity> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(CalendarInfoDto::new)
                .collect(Collectors.toList());
    }

    public static List<CalendarInfoDto> fromGroupUserEntityList(List<GroupUserEntity> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(CalendarInfoDto::new)
                .collect(Collectors.toList());
    }
}
