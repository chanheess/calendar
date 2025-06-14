package com.chpark.chcalendar.service.calendar;


import com.chpark.chcalendar.dto.calendar.CalendarColorDto;
import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.repository.CalendarRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserCalendarService implements CalendarService {

    private final CalendarRepository calendarRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public CalendarDto.Response create(long userId, String title) {
        int maxCalendarCount = 10;

        if (maxCalendarCount <= calendarRepository.findByUserIdAndCategory(userId, CalendarCategory.USER).size()) {
            throw new IllegalArgumentException("You have reached the maximum limit for creating calendar.");
        }

        CalendarEntity result = new CalendarEntity(title, userId, CalendarCategory.USER);
        calendarRepository.save(result);

        return new CalendarDto.Response(result);
    }

    @Override
    public List<CalendarDto.Response> findCalendarList(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        return CalendarDto.Response.fromCalendarEntityList(
                calendarRepository.findByUserIdAndCategory(userId, CalendarCategory.USER)
        );
    }

    @Override
    public CalendarColorDto changeColor(long userId, CalendarColorDto calendarColorDto) {
        CalendarEntity calendar = calendarRepository.findByIdAndUserId(calendarColorDto.getCalendarId(), userId).orElseThrow(
                () -> new EntityNotFoundException("Calendar not found.")
        );

        calendar.getCalendarSetting().setColor(calendarColorDto.getColor());
        calendarRepository.save(calendar);
        return new CalendarColorDto(calendar.getId(), calendar.getCalendarSetting().getColor(), CalendarCategory.USER);
    }

    public List<Long> findCalendarIdList(long userId) {
        return calendarRepository.findIdByUserIdAndCategory(userId, CalendarCategory.USER);
    }

    public void checkCalendarAdminUser(long calendarId, long userId) {
        calendarRepository.findByIdAndUserId(calendarId, userId).orElseThrow(
                () -> new EntityNotFoundException("You do not have permission.")
        );
    }


}
