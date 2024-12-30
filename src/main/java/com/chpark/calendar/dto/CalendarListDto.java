package com.chpark.calendar.dto;

import com.chpark.calendar.dto.group.GroupDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CalendarListDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        List<GroupDto> groupInfo;
        List<CalendarInfoDto.Response> calendarInfo;
    }
}
