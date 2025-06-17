package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.dto.calendar.CalendarSettingDto;
import com.chpark.chcalendar.entity.calendar.CalendarSettingEntity;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.calendar.CalendarSettingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public abstract class CalendarService {

    protected final CalendarRepository calendarRepository;
    protected final CalendarSettingRepository calendarSettingRepository;

    public abstract CalendarDto.Response create(long userId, String title);
    public abstract List<CalendarDto.Response> findCalendarList(HttpServletRequest request);
    public abstract List<Long> findCalendarIdList(long userId);

    @Transactional
    public CalendarSettingDto updateSetting(long userId, CalendarSettingDto calendarSettingDto) {
        CalendarSettingEntity calendarSettingEntity = calendarSettingRepository.findByCalendarIdAndUserId(calendarSettingDto.getCalendarId(), userId).orElseThrow(
                () -> new EntityNotFoundException("캘린더를 찾을 수 없습니다.")
        );

        if (calendarSettingDto.getColor() != null) {
            calendarSettingEntity.setColor(calendarSettingDto.getColor());
        }

        if (calendarSettingDto.getChecked() != null) {
            calendarSettingEntity.setChecked(calendarSettingDto.getChecked());
        }
        
        return CalendarSettingDto.builder()
                .calendarId(calendarSettingEntity.getCalendar().getId())
                .color(calendarSettingEntity.getColor())
                .category(calendarSettingEntity.getCalendar().getCategory())
                .checked(calendarSettingEntity.getChecked())
                .build();
    }
}
