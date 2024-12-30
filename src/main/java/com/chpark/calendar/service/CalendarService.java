package com.chpark.calendar.service;

import com.chpark.calendar.dto.CalendarInfoDto;
import com.chpark.calendar.dto.CalendarListDto;
import com.chpark.calendar.dto.group.GroupDto;
import com.chpark.calendar.entity.CalendarInfoEntity;
import com.chpark.calendar.enumClass.CalendarCategory;
import com.chpark.calendar.repository.CalendarInfoRepository;
import com.chpark.calendar.service.group.GroupUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CalendarService {

    private final CalendarInfoRepository calendarInfoRepository;
    private final GroupUserService groupUserService;

    public String create(long userId, String title, CalendarCategory category) {

        CalendarInfoEntity entity = new CalendarInfoEntity(title, userId, category);
        calendarInfoRepository.save(entity);

        return entity.getTitle();
    }

    public List<CalendarInfoDto.Response> getCalendarInfoList(long userId) {
        return CalendarInfoDto.Response.fromCalendarEntityList(
                calendarInfoRepository.findByUserId(userId)
        );
    }

    @Transactional
    public CalendarListDto.Response getCalendarList(long userId) {

        List<GroupDto> groupList = groupUserService.findMyGroup(userId);
        List<CalendarInfoDto.Response> calendarInfoList = this.getCalendarInfoList(userId);

        return new CalendarListDto.Response(groupList, calendarInfoList);
    }
}
