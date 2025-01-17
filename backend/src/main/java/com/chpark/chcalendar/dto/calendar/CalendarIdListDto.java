package com.chpark.chcalendar.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CalendarIdListDto {

    List<Long> calendarIdList;
}
