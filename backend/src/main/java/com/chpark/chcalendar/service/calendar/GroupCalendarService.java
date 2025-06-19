package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.CalendarMemberRole;
import com.chpark.chcalendar.repository.calendar.CalendarQueryRepository;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.calendar.CalendarSettingRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupCalendarService extends CalendarService {

    private final CalendarMemberService calendarMemberService;
    private final CalendarQueryRepository calendarQueryRepository;

    public GroupCalendarService(CalendarRepository calendarRepository, CalendarSettingRepository calendarSettingRepository, JwtTokenProvider jwtTokenProvider, CalendarMemberService calendarMemberService, CalendarQueryRepository calendarQueryRepository) {
        super(calendarRepository, calendarSettingRepository, jwtTokenProvider);
        this.calendarMemberService = calendarMemberService;
        this.calendarQueryRepository = calendarQueryRepository;
    }

    @Override
    public CalendarDto.Response create(long userId, String title) {
        int maxAdminCount = 10;

        if (maxAdminCount <= calendarRepository.countAdminGroups(userId)) {
            throw new IllegalArgumentException("You have reached the maximum limit for creating groups.");
        }

        CalendarEntity calendarEntity = new CalendarEntity(title, userId, CalendarCategory.GROUP);
        calendarRepository.save(calendarEntity);

        calendarMemberService.create(calendarEntity, calendarEntity.getUserId(), CalendarMemberRole.ADMIN);

        return CalendarDto.Response.builder()
                .id(calendarEntity.getId())
                .title(calendarEntity.getTitle())
                .category(calendarEntity.getCategory())
                .color(calendarEntity.getCalendarSettings().get(0).getColor())
                .build();
    }

    @Override
    public List<CalendarDto.Response> findCalendarList(long userId) {
        return calendarQueryRepository.findGroupCalendarsByUserId(userId);
    }

    @Override
    public List<Long> findCalendarIdList(long userId) {
        return calendarMemberService.findCalendarIdList(userId);
    }
}
