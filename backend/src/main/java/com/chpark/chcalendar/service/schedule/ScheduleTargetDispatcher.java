package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.dto.schedule.ScheduleTargetActionDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.enumClass.CRUDAction;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.event.schedule.GoogleScheduleCreateEvent;
import com.chpark.chcalendar.event.schedule.GoogleScheduleDeleteEvent;
import com.chpark.chcalendar.event.schedule.GoogleScheduleUpdateEvent;
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
    public ScheduleTargetActionDto getTargetCreateAction(ScheduleDto.Request scheduleDto, HttpServletRequest request) {
        if (scheduleDto == null || scheduleDto.getScheduleDto() == null) {
            throw new IllegalArgumentException("일정 정보가 없습니다");
        }

        CalendarEntity calendar = calendarRepository.findById(scheduleDto.getScheduleDto().getCalendarId()).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 캘린더입니다.")
        );
        CalendarCategory category = calendar.getCategory();

        if (category.isExternalProvider()) {
            return new ScheduleTargetActionDto(
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
    public ScheduleTargetActionDto getTargetUpdateAction(ScheduleDto.Request scheduleDto, HttpServletRequest request) {
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

        ScheduleTargetActionDto result = null;

        if (CalendarCategory.USER == currentCategory && newCategory.isExternalProvider()) {
            result = new ScheduleTargetActionDto(
                    newCategory,
                    CRUDAction.CREATE,
                    newCalendar.getCalendarProvider().getProviderId(),
                    null,
                    getAccessToken(newCategory, request)
            );
        } else if (currentCategory.isExternalProvider() && CalendarCategory.USER == newCategory) {
            result = new ScheduleTargetActionDto(
                    currentCategory,
                    CRUDAction.DELETE,
                    currentCalendar.getCalendarProvider().getProviderId(),
                    scheduleEntity.getProviderId(),
                    getAccessToken(currentCategory, request)
            );
        } else if (newCategory == currentCategory && currentCategory.isExternalProvider()) {
            result = new ScheduleTargetActionDto(
                    currentCategory,
                    CRUDAction.UPDATE,
                    newCalendar.getCalendarProvider().getProviderId(),
                    scheduleEntity.getProviderId(),
                    getAccessToken(currentCategory, request)
            );
        } else if (newCategory != currentCategory && currentCategory.isExternalProvider() && newCategory.isExternalProvider()) {
            //외부와 외부 비교 추후 추가: 이 때는 List<ScheduleTargetActionDto>로 변경해야될듯
        }

        return result;
    }

    public ScheduleTargetActionDto getDeleteAction(Long scheduleId, Long calendarId, HttpServletRequest request) {
        CalendarEntity calendar = calendarRepository.findById(calendarId).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 캘린더입니다.")
        );
        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ScheduleException("Not found schedule")
        );
        CalendarCategory category = calendar.getCategory();

        if (category.isExternalProvider()) {
            return new ScheduleTargetActionDto(
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
    public void handleTargetScheduleAction(ScheduleTargetActionDto scheduleTargetActionDto, ScheduleDto.Request scheduleDto) {
        switch (scheduleTargetActionDto.getAction()) {
            case CREATE -> {
                createTargetSchedule(scheduleTargetActionDto, scheduleDto);
            }
            case UPDATE -> {
                updateTargetSchedule(scheduleTargetActionDto, scheduleDto);
            }
            case DELETE -> {
                deleteTargetSchedule(scheduleTargetActionDto);
            }
        }
    }

    @Transactional
    public void createTargetSchedule(ScheduleTargetActionDto scheduleTargetActionDto, ScheduleDto.Request scheduleDto) {
        ScheduleDto localSchedule = scheduleDto.getScheduleDto();
        CalendarCategory category = scheduleTargetActionDto.getCategory();
        String providerId = scheduleTargetActionDto.getCalendarProviderId();
        String accessToken = scheduleTargetActionDto.getAccessToken();

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
    public void updateTargetSchedule(ScheduleTargetActionDto scheduleTargetActionDto, ScheduleDto.Request scheduleDto) {
        ScheduleDto localSchedule = scheduleDto.getScheduleDto();
        CalendarCategory category = scheduleTargetActionDto.getCategory();
        String calendarProviderId = scheduleTargetActionDto.getCalendarProviderId();
        String scheduleProviderId = scheduleTargetActionDto.getScheduleProviderId();
        String accessToken = scheduleTargetActionDto.getAccessToken();

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
    public void deleteTargetSchedule(ScheduleTargetActionDto scheduleTargetActionDto) {
        CalendarCategory category = scheduleTargetActionDto.getCategory();
        String calendarProviderId = scheduleTargetActionDto.getCalendarProviderId();
        String scheduleProviderId = scheduleTargetActionDto.getScheduleProviderId();
        String accessToken = scheduleTargetActionDto.getAccessToken();

        if (category == CalendarCategory.GOOGLE) {
            eventPublisher.publishEvent(new GoogleScheduleDeleteEvent(
                    calendarProviderId,
                    scheduleProviderId,
                    accessToken
            ));
        }
    }


}
