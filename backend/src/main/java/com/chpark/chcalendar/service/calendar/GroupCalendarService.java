package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.dto.calendar.CalendarSettingDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.calendar.CalendarSettingEntity;
import com.chpark.chcalendar.enumClass.CRUDAction;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.CalendarMemberRole;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.exception.authorization.GroupAuthorizationException;
import com.chpark.chcalendar.repository.calendar.CalendarQueryRepository;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.calendar.CalendarSettingRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void checkAuthority(CRUDAction action, long userId, long createdUserId, long calendarId) {
        CalendarMemberRole result = null;

        switch (action) {
            case CREATE, UPDATE -> {
                result = CalendarMemberRole.USER;
            }
            case READ -> {
                result = CalendarMemberRole.READ;
            }
            case DELETE -> {
                if (userId != createdUserId) {
                    result = CalendarMemberRole.SUB_ADMIN;
                    break;
                }

                result = CalendarMemberRole.USER;
            }
        }

        calendarMemberService.checkCalendarMemberAuthority(userId, calendarId, result);
    }

    @Transactional
    @Override
    public CalendarSettingDto updateSetting(HttpServletRequest request, CalendarSettingDto calendarSettingDto) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarSettingEntity calendarSettingEntity = calendarSettingRepository.findByCalendarIdAndUserId(calendarSettingDto.getCalendarId(), userId).orElseThrow(
                () -> new EntityNotFoundException("캘린더를 찾을 수 없습니다.")
        );

        if (calendarSettingDto.getTitle() != null) {
            calendarMemberService.checkCalendarMemberAuthority(userId, calendarSettingDto.getCalendarId(), CalendarMemberRole.ADMIN);
            calendarSettingEntity.getCalendar().setTitle(calendarSettingDto.getTitle());
        }

        if (calendarSettingDto.getColor() != null) {
            calendarSettingEntity.setColor(calendarSettingDto.getColor());
        }

        if (calendarSettingDto.getChecked() != null) {
            calendarSettingEntity.setChecked(calendarSettingDto.getChecked());
        }

        return CalendarSettingDto.builder()
                .calendarId(calendarSettingEntity.getCalendar().getId())
                .title(calendarSettingEntity.getCalendar().getTitle())
                .color(calendarSettingEntity.getColor())
                .category(calendarSettingEntity.getCalendar().getCategory())
                .checked(calendarSettingEntity.getChecked())
                .build();
    }
}
