package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.dto.schedule.ScheduleProviderActionDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.enumClass.CRUDAction;
import com.chpark.chcalendar.event.schedule.google.GoogleScheduleCreateEvent;
import com.chpark.chcalendar.event.schedule.google.GoogleScheduleDeleteEvent;
import com.chpark.chcalendar.event.schedule.google.GoogleScheduleUpdateEvent;
import com.chpark.chcalendar.exception.ScheduleException;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.utility.CookieUtility;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ScheduleTargetDispatcher {

    private final CalendarRepository calendarRepository;
    private final ScheduleRepository scheduleRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ScheduleProviderActionDto getTargetCreateAction(ScheduleDto.Request scheduleDto, HttpServletRequest request) {
        if (scheduleDto == null || scheduleDto.getScheduleDto() == null) {
            throw new IllegalArgumentException("일정 정보가 없습니다");
        }

        CalendarEntity calendar = calendarRepository.findById(scheduleDto.getScheduleDto().getCalendarId()).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 캘린더입니다.")
        );
        CalendarCategory category = calendar.getCategory();

        if (category.isExternalProvider()) {
            return new ScheduleProviderActionDto(
                    category,
                    CRUDAction.CREATE,
                    calendar.getCalendarProvider().getProviderId(),
                    null,
                    getAccessToken(category, request)
            );
        }

        return null;
    }

    @Transactional
    public ScheduleProviderActionDto getTargetUpdateAction(ScheduleDto.Request scheduleDto, HttpServletRequest request) {
        if (scheduleDto == null || scheduleDto.getScheduleDto() == null) {
            throw new IllegalArgumentException("일정 정보가 없습니다");
        }

        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleDto.getScheduleDto().getId()).orElseThrow(
                () -> new ScheduleException("Not found schedule")
        );
        CalendarEntity currentCalendar = calendarRepository.findById(scheduleEntity.getCalendarId()).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 캘린더입니다.")
        );
        CalendarEntity newCalendar = calendarRepository.findById(scheduleDto.getScheduleDto().getCalendarId()).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 캘린더입니다.")
        );

        CalendarCategory currentCategory = currentCalendar.getCategory();
        CalendarCategory newCategory = newCalendar.getCategory();

        ScheduleProviderActionDto result = null;

        if (CalendarCategory.USER == currentCategory && newCategory.isExternalProvider()) {
            result = new ScheduleProviderActionDto(
                    newCategory,
                    CRUDAction.CREATE,
                    newCalendar.getCalendarProvider().getProviderId(),
                    null,
                    getAccessToken(newCategory, request)
            );
        } else if (currentCategory.isExternalProvider() && CalendarCategory.USER == newCategory) {
            result = new ScheduleProviderActionDto(
                    currentCategory,
                    CRUDAction.DELETE,
                    currentCalendar.getCalendarProvider().getProviderId(),
                    scheduleEntity.getProviderId(),
                    getAccessToken(currentCategory, request)
            );
        } else if (newCategory == currentCategory && currentCategory.isExternalProvider()) {
            result = new ScheduleProviderActionDto(
                    currentCategory,
                    CRUDAction.UPDATE,
                    newCalendar.getCalendarProvider().getProviderId(),
                    scheduleEntity.getProviderId(),
                    getAccessToken(currentCategory, request)
            );
        } else if (newCategory != currentCategory && currentCategory.isExternalProvider() && newCategory.isExternalProvider()) {
            //외부와 외부 비교 추후 추가: 이 때는 List<ScheduleProviderActionDto>로 변경해야될듯
        }

        return result;
    }

    public ScheduleProviderActionDto getDeleteAction(Long scheduleId, Long calendarId, HttpServletRequest request) {
        CalendarEntity calendar = calendarRepository.findById(calendarId).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 캘린더입니다.")
        );
        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ScheduleException("Not found schedule")
        );
        CalendarCategory category = calendar.getCategory();

        if (category.isExternalProvider()) {
            return new ScheduleProviderActionDto(
                    category,
                    CRUDAction.DELETE,
                    calendar.getCalendarProvider().getProviderId(),
                    scheduleEntity.getProviderId(),
                    getAccessToken(category, request)
            );
        }

        return null;
    }

    public String getAccessToken(CalendarCategory category, HttpServletRequest request) {
        if (category == CalendarCategory.GOOGLE) {
            return CookieUtility.getToken(request, JwtTokenType.GOOGLE_ACCESS);
        }
        return null;
    }

    @Transactional
    public void handleTargetScheduleAction(ScheduleProviderActionDto scheduleProviderActionDto, ScheduleDto.Response scheduleDto) {
        switch (scheduleProviderActionDto.getAction()) {
            case CREATE -> {
                createTargetSchedule(scheduleProviderActionDto, scheduleDto);
            }
            case UPDATE -> {
                updateTargetSchedule(scheduleProviderActionDto, scheduleDto);
            }
            case DELETE -> {
                deleteTargetSchedule(scheduleProviderActionDto);
            }
        }
    }

    @Transactional
    public void createTargetSchedule(ScheduleProviderActionDto scheduleProviderActionDto, ScheduleDto.Response scheduleDto) {
        ScheduleDto localSchedule = scheduleDto.getScheduleDto();
        CalendarCategory category = scheduleProviderActionDto.getCategory();
        String providerId = scheduleProviderActionDto.getCalendarProviderId();
        String accessToken = scheduleProviderActionDto.getAccessToken();

        if (category == CalendarCategory.GOOGLE) {
            eventPublisher.publishEvent(new GoogleScheduleCreateEvent(
                    localSchedule.getTitle(),
                    localSchedule.getDescription(),
                    localSchedule.getStartAt(),
                    localSchedule.getEndAt(),
                    localSchedule.getId(),
                    providerId,
                    scheduleDto.getNotificationDto().stream().toList(),
                    accessToken
            ));
        }
    }

    @Transactional
    public void updateTargetSchedule(ScheduleProviderActionDto scheduleProviderActionDto, ScheduleDto.Response scheduleDto) {
        ScheduleDto localSchedule = scheduleDto.getScheduleDto();
        CalendarCategory category = scheduleProviderActionDto.getCategory();
        String calendarProviderId = scheduleProviderActionDto.getCalendarProviderId();
        String scheduleProviderId = scheduleProviderActionDto.getScheduleProviderId();
        String accessToken = scheduleProviderActionDto.getAccessToken();

        if (category == CalendarCategory.GOOGLE) {
            eventPublisher.publishEvent(new GoogleScheduleUpdateEvent(
                    localSchedule.getTitle(),
                    localSchedule.getDescription(),
                    localSchedule.getStartAt(),
                    localSchedule.getEndAt(),
                    calendarProviderId,
                    scheduleProviderId,
                    scheduleDto.getNotificationDto().stream().toList(),
                    accessToken
            ));
        }
    }

    @Transactional
    public void deleteTargetSchedule(ScheduleProviderActionDto scheduleProviderActionDto) {
        CalendarCategory category = scheduleProviderActionDto.getCategory();
        String calendarProviderId = scheduleProviderActionDto.getCalendarProviderId();
        String scheduleProviderId = scheduleProviderActionDto.getScheduleProviderId();
        String accessToken = scheduleProviderActionDto.getAccessToken();

        if (category == CalendarCategory.GOOGLE) {
            eventPublisher.publishEvent(new GoogleScheduleDeleteEvent(
                    calendarProviderId,
                    scheduleProviderId,
                    accessToken
            ));
        }
    }


}
