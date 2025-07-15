package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.dto.calendar.CalendarSettingDto;
import com.chpark.chcalendar.entity.calendar.CalendarSettingEntity;
import com.chpark.chcalendar.enumClass.CRUDAction;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.event.schedule.ScheduleDeleteEvent;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.calendar.CalendarSettingRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public abstract class CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarService.class);
    protected final CalendarRepository calendarRepository;
    protected final CalendarSettingRepository calendarSettingRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private final ApplicationEventPublisher eventPublisher;

    public abstract CalendarDto.Response create(long userId, String title);
    public abstract List<CalendarDto.Response> findCalendarList(long userId);
    public abstract List<Long> findCalendarIdList(long userId);
    public abstract void checkAuthority(CRUDAction action, long userId, long calendarId);

    @Transactional
    public void deleteCalendar(long userId, long calendarId) {
        //일정 삭제 이벤트 발행
        eventPublisher.publishEvent(new ScheduleDeleteEvent(
                userId,
                calendarId
        ));
    }

    @Transactional
    public CalendarSettingDto updateSetting(HttpServletRequest request, CalendarSettingDto calendarSettingDto) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarSettingEntity calendarSettingEntity = calendarSettingRepository.findByUserIdAndCalendarId(userId, calendarSettingDto.getCalendarId()).orElseThrow(
                () -> new EntityNotFoundException("캘린더를 찾을 수 없습니다.")
        );

        if (calendarSettingDto.getTitle() != null) {
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
