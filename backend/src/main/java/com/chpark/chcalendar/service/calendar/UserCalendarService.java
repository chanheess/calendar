package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.enumClass.CRUDAction;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.repository.calendar.CalendarQueryRepository;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.calendar.CalendarSettingRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserCalendarService extends CalendarService {

    private final CalendarQueryRepository calendarQueryRepository;

    public UserCalendarService(CalendarRepository calendarRepository, CalendarSettingRepository calendarSettingRepository, JwtTokenProvider jwtTokenProvider, ApplicationEventPublisher eventPublisher, CalendarQueryRepository calendarQueryRepository) {
        super(calendarRepository, calendarSettingRepository, jwtTokenProvider, eventPublisher);
        this.calendarQueryRepository = calendarQueryRepository;
    }

    @Override
    public CalendarDto.Response create(long userId, String title) {
        int maxCalendarCount = 10;

        if (maxCalendarCount <= calendarRepository.findByUserIdAndCategory(userId, CalendarCategory.USER).size()) {
            throw new IllegalArgumentException("You have reached the maximum limit for creating calendars.");
        }

        CalendarEntity result = CalendarEntity.builder()
                .title(title)
                .userId(userId)
                .category(CalendarCategory.USER)
                .build();
        calendarRepository.save(result);

        return CalendarDto.Response.builder()
                .id(result.getId())
                .title(result.getTitle())
                .category(result.getCategory())
                .color(result.getCalendarSettings().get(0).getColor())
                .build();
    }

    @Override
    public List<CalendarDto.Response> findCalendarList(long userId) {
        return calendarQueryRepository.findUserCalendarList(userId);
    }

    public List<Long> findCalendarIdList(long userId) {
        return calendarRepository.findIdByUserIdAndCategory(userId, CalendarCategory.USER);
    }

    @Override
    public void checkAuthority(CRUDAction action, long userId, long calendarId) {
        calendarRepository.findByIdAndUserId(calendarId, userId).orElseThrow(
                () -> new EntityNotFoundException("You do not have permission.")
        );
    }

    @Override
    public void deleteCalendar(long userId, long calendarId) {
        super.deleteCalendar(userId, calendarId);

        Optional<CalendarEntity> calendarEntity = calendarRepository.findByIdAndUserId(calendarId, userId);

        if (calendarEntity.isEmpty()) {
            return;
        }

        calendarSettingRepository.deleteAll(calendarEntity.get().getCalendarSettings());
        calendarRepository.delete(calendarEntity.get());
    }
}
