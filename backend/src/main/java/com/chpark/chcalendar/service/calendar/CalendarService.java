package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.dto.calendar.CalendarSettingDto;
import com.chpark.chcalendar.entity.calendar.CalendarSettingEntity;
import com.chpark.chcalendar.enumClass.CRUDAction;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.enumClass.CalendarMemberRole;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.calendar.CalendarSettingRepository;
import com.chpark.chcalendar.repository.calendar.CalendarMemberRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.exception.authorization.GroupAuthorizationException;
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

    protected static final Logger log = LoggerFactory.getLogger(CalendarService.class);
    protected final CalendarRepository calendarRepository;
    protected final CalendarSettingRepository calendarSettingRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CalendarMemberRepository calendarMemberRepository;

    private final ApplicationEventPublisher eventPublisher;

    public CalendarService(CalendarRepository calendarRepository, CalendarSettingRepository calendarSettingRepository, JwtTokenProvider jwtTokenProvider, ApplicationEventPublisher eventPublisher, CalendarMemberRepository calendarMemberRepository) {
        this.calendarRepository = calendarRepository;
        this.calendarSettingRepository = calendarSettingRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.eventPublisher = eventPublisher;
        this.calendarMemberRepository = calendarMemberRepository;
    }

    public abstract CalendarDto.Response create(long userId, String title);
    public abstract List<CalendarDto.Response> findCalendarList(long userId);
    public abstract List<Long> findCalendarIdList(long userId);
    public abstract void checkAuthority(CRUDAction action, long userId, long calendarId);

    @Transactional
    public abstract void deleteCalendar(long userId, long calendarId);

    @Transactional
    public CalendarSettingDto updateSetting(HttpServletRequest request, CalendarSettingDto calendarSettingDto) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarSettingEntity calendarSettingEntity = calendarSettingRepository.findByUserIdAndCalendarId(userId, calendarSettingDto.getCalendarId()).orElseThrow(
                () -> new EntityNotFoundException("캘린더를 찾을 수 없습니다.")
        );

        // 이름 변경 시 권한 확인
        if (calendarSettingDto.getTitle() != null) {
            // 그룹 캘린더인 경우 권한 확인
            if (calendarSettingEntity.getCalendar().getCategory().name().equals("GROUP")) {
                var memberEntity = calendarMemberRepository.findByUserIdAndCalendarId(userId, calendarSettingDto.getCalendarId())
                    .orElseThrow(() -> new GroupAuthorizationException("캘린더에 대한 권한이 없습니다."));
                
                if (memberEntity.getRole() == CalendarMemberRole.USER) {
                    throw new GroupAuthorizationException("이름 변경 권한이 없습니다. ADMIN 또는 SUB_ADMIN 권한이 필요합니다.");
                }
            }
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
