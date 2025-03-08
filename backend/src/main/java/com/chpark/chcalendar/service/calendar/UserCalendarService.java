package com.chpark.chcalendar.service.calendar;


import com.chpark.chcalendar.dto.calendar.CalendarColorDto;
import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.entity.CalendarInfoEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.repository.CalendarInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserCalendarService implements CalendarService {

    private final CalendarInfoRepository calendarInfoRepository;

    @Override
    public CalendarInfoDto.Response create(long userId, String title) {
        int maxCalendarCount = 10;

        if (maxCalendarCount <= calendarInfoRepository.findByAdminIdAndCategory(userId, CalendarCategory.USER).size()) {
            throw new IllegalArgumentException("You have reached the maximum limit for creating calendar.");
        }

        CalendarInfoEntity result = new CalendarInfoEntity(title, userId, CalendarCategory.USER);
        calendarInfoRepository.save(result);

        return new CalendarInfoDto.Response(result);
    }

    @Override
    public List<CalendarInfoDto.Response> findCalendarList(long userId) {

        return CalendarInfoDto.Response.fromCalendarEntityList(
                calendarInfoRepository.findByAdminIdAndCategory(userId, CalendarCategory.USER)
        );
    }

    @Override
    public CalendarColorDto changeColor(long userId, CalendarColorDto calendarColorDto) {
        CalendarInfoEntity calendarInfo = calendarInfoRepository.findByIdAndAdminId(userId, calendarColorDto.getCalendarId()).orElseThrow(
                () -> new EntityNotFoundException("없는 캘린더다.")
        );

        calendarInfo.setColor(calendarColorDto.getColor());
        return new CalendarColorDto(calendarInfo.getId(), calendarInfo.getColor());
    }

    public List<Long> findCalendarIdList(long userId) {
        return calendarInfoRepository.findIdByAdminIdAndCategory(userId, CalendarCategory.USER);
    }

    public void checkCalendarAdminUser(long calendarId, long userId) {
        calendarInfoRepository.findByIdAndAdminId(calendarId, userId).orElseThrow(
                () -> new EntityNotFoundException("권한이 없습니다.")
        );
    }


}
