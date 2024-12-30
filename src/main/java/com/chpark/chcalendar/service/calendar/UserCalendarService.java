package com.chpark.chcalendar.service.calendar;


import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.dto.calendar.CalendarListDto;
import com.chpark.chcalendar.entity.CalendarInfoEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.repository.CalendarInfoRepository;
import com.chpark.chcalendar.service.group.GroupUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserCalendarService implements CalendarService {

    private final CalendarInfoRepository calendarInfoRepository;
    private final GroupUserService groupUserService;

    @Override
    public CalendarInfoDto create(long userId, String title) {

        CalendarInfoEntity result = new CalendarInfoEntity(title, userId, CalendarCategory.USER);
        calendarInfoRepository.save(result);

        return new CalendarInfoDto(result);
    }

    @Override
    public List<CalendarInfoDto> findCalendarList(long userId) {

        return CalendarInfoDto.fromCalendarEntityList(
                calendarInfoRepository.findByAdminIdAndCategory(userId, CalendarCategory.USER)
        );
    }
}
