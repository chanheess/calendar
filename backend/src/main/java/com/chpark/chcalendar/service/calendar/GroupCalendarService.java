package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.enumClass.CRUDAction;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.CalendarMemberRole;
import com.chpark.chcalendar.repository.calendar.CalendarQueryRepository;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.calendar.CalendarSettingRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupCalendarService extends CalendarService {

    private final CalendarMemberService calendarMemberService;
    private final CalendarQueryRepository calendarQueryRepository;

    public GroupCalendarService(CalendarRepository calendarRepository, CalendarSettingRepository calendarSettingRepository, JwtTokenProvider jwtTokenProvider, ApplicationEventPublisher eventPublisher, CalendarMemberService calendarMemberService, CalendarQueryRepository calendarQueryRepository) {
        super(calendarRepository, calendarSettingRepository, jwtTokenProvider, eventPublisher);
        this.calendarMemberService = calendarMemberService;
        this.calendarQueryRepository = calendarQueryRepository;
    }

    @Override
    public CalendarDto.Response create(long userId, String title) {
        int maxAdminCount = 10;

        if (maxAdminCount <= calendarRepository.countAdminGroups(userId)) {
            throw new IllegalArgumentException("그룹 생성 최대 한도에 도달했습니다.");
        }

        CalendarEntity calendarEntity = CalendarEntity.builder()
                .title(title)
                .userId(userId)
                .category(CalendarCategory.GROUP)
                .build();
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

    @Override
    public void checkAuthority(CRUDAction action, long userId, long calendarId) {
        calendarMemberService.checkCalendarMemberAuthority(userId, calendarId, CalendarMemberRole.USER);
    }

    @Override
    public void deleteCalendar(long userId, long calendarId) {
        calendarMemberService.removeGroupMembership(userId, calendarId);
        calendarSettingRepository.deleteByUserIdAndCalendarId(userId, calendarId);

        if (calendarMemberService.getMemberCount(calendarId) == 0) {
            calendarRepository.deleteById(calendarId);
        }
    }


}
