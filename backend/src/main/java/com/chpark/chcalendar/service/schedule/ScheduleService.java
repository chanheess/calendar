package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.CursorPage;
import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.dto.schedule.ScheduleGroupDto;
import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.chpark.chcalendar.dto.schedule.ScheduleRepeatDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.enumClass.CRUDAction;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.exception.CustomException;
import com.chpark.chcalendar.exception.ScheduleException;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleNotificationRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleQueryRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepeatRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.service.calendar.CalendarService;
import com.chpark.chcalendar.utility.CalendarUtility;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;


@RequiredArgsConstructor
@Service
public class ScheduleService {

    protected final ScheduleRepository scheduleRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final ScheduleRepeatRepository scheduleRepeatRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;

    protected final ScheduleRepeatService scheduleRepeatService;
    protected final ScheduleNotificationService scheduleNotificationService;
    protected final ScheduleGroupService scheduleGroupService;

    private final Map<CalendarCategory, CalendarService> calendarServiceMap;
    private final CalendarRepository calendarRepository;

    @Transactional
    public ScheduleDto create(ScheduleDto scheduleDto, long userId) {
        scheduleDto.validateScheduleDto();

        //빈 제목일 경우 제목 없음으로 처리
        scheduleDto.setTitle(scheduleDto.getTitle().isEmpty() ? "Untitled" : scheduleDto.getTitle());
        scheduleDto.setUserId(userId);

        ScheduleEntity savedEntity = new ScheduleEntity(scheduleDto);
        savedEntity = scheduleRepository.save(savedEntity);

        return new ScheduleDto(savedEntity);
    }

    @Transactional
    public ScheduleDto.Response createByForm(ScheduleDto.Request scheduleDto, long userId) {
        dispatchAuthCheck(CRUDAction.CREATE, userId, userId, null, scheduleDto);

        ScheduleDto resultSchedule = this.create(scheduleDto.getScheduleDto(), userId);
        List<ScheduleNotificationDto> resultNotifications = scheduleNotificationService.create(userId, resultSchedule.getId(), scheduleDto.getNotificationDto().stream().toList());
        List<ScheduleGroupDto> groupSchedule = scheduleGroupService.createScheduleGroup(resultSchedule, scheduleDto.getGroupDto().stream().toList());
        ScheduleRepeatDto resultRepeat = scheduleRepeatService.create(resultSchedule.getId(), scheduleDto.getRepeatDto(), userId);

        return new ScheduleDto.Response(resultSchedule, resultNotifications, resultRepeat, groupSchedule);
    }

    @Transactional
    public List<ScheduleDto> findSchedulesByTitle(String title, long userId) {
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findByTitleContainingAndUserId(title, userId));
    }

    @Transactional
    public ScheduleDto update(long scheduleId, ScheduleDto scheduleDto) {
        return this.update(scheduleId, scheduleDto, false);
    }

    @Transactional
    public ScheduleDto update(long scheduleId, ScheduleDto scheduleDto, boolean isRepeat) {
        ScheduleEntity schedule = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found")
        );

        if (scheduleDto.getTitle() != null) {
            schedule.setTitle(scheduleDto.getTitle().isEmpty() ? "Untitled" : scheduleDto.getTitle());
        }
        if (scheduleDto.getDescription() != null) {
            schedule.setDescription(scheduleDto.getDescription());
        }
        if (scheduleDto.getStartAt() != null) {
            scheduleDto.validateScheduleDto();
            schedule.setStartAt(scheduleDto.getStartAt());
        }
        if (scheduleDto.getEndAt() != null) {
            scheduleDto.validateScheduleDto();
            schedule.setEndAt(scheduleDto.getEndAt());
        }
        //반복 수정시에 repeatId를 null로 비워준다.
        if (isRepeat) {
            schedule.setRepeatId(null);
        }

        if (scheduleDto.getCalendarId() != null) {
            schedule.setCalendarId(scheduleDto.getCalendarId());
        }

        return new ScheduleDto(scheduleRepository.save(schedule));
    }

    @Transactional
    public ScheduleDto.Response updateSchedule(long scheduleId, boolean isRepeatChecked, ScheduleDto.Request scheduleDto, long userId) {
        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ScheduleException("Not found schedule")
        );

        if (scheduleEntity.getRepeatId() != null) {
            throw new ScheduleException("has repeat-id");
        }

        dispatchAuthCheck(CRUDAction.UPDATE, userId, scheduleEntity.getUserId(), scheduleEntity.getId(), scheduleDto);

        ScheduleDto updateDto = this.update(scheduleId, scheduleDto.getScheduleDto());
        List<ScheduleNotificationDto> updateNotificationDto = scheduleNotificationService.update(userId, scheduleId, scheduleDto.getNotificationDto().stream().toList());
        ScheduleRepeatDto updateRepeatDto = null;
        List<ScheduleGroupDto> groupScheduleDto = scheduleGroupService.updateScheduleGroup(userId, updateDto, scheduleDto.getGroupDto().stream().toList());

        if (isRepeatChecked){
            updateRepeatDto = scheduleRepeatService.create(scheduleId, scheduleDto.getRepeatDto(), userId);
        }

        return new ScheduleDto.Response(updateDto, updateNotificationDto, updateRepeatDto, groupScheduleDto);
    }

    @Transactional
    public ScheduleDto.Response updateRepeatSchedule(long scheduleId, boolean isRepeatChecked, ScheduleDto.Request scheduleDto, long userId) {
        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ScheduleException("Not found schedule")
        );

        if (scheduleEntity.getRepeatId() == null) {
            throw new ScheduleException("Not repeat schedule");
        }

        dispatchAuthCheck(CRUDAction.UPDATE, userId, scheduleEntity.getUserId(), scheduleEntity.getId(), scheduleDto);

        //기존 반복 일정 삭제
        this.deleteFutureRepeatSchedules(scheduleId, userId);

        //일정 및 알림 업데이트
        ScheduleDto updateDto = this.update(scheduleId, scheduleDto.getScheduleDto(), true);
        List<ScheduleNotificationDto> updateNotificationDto = scheduleNotificationService.update(userId, scheduleId, scheduleDto.getNotificationDto().stream().toList());
        List<ScheduleGroupDto> groupSchedule = scheduleGroupService.updateScheduleGroup(
                userId, new ScheduleDto(scheduleEntity), scheduleDto.getGroupDto().stream().toList()
        );

        ScheduleRepeatDto updateRepeatDto = null;

        if (isRepeatChecked) {
            updateRepeatDto = scheduleRepeatService.create(scheduleId, scheduleDto.getRepeatDto(), userId);
        }
        return new ScheduleDto.Response(updateDto, updateNotificationDto, updateRepeatDto, groupSchedule);
    }

    @Transactional
    public ScheduleDto.Response updateRepeatCurrentOnlySchedule(long scheduleId, ScheduleDto.Request scheduleDto, long userId) {
        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ScheduleException("Not found schedule")
        );

        if (scheduleEntity.getRepeatId() == null) {
            throw new ScheduleException("Not repeat schedule");
        }

        dispatchAuthCheck(CRUDAction.UPDATE, userId, scheduleEntity.getUserId(), scheduleEntity.getId(), scheduleDto);

        List<ScheduleGroupDto> groupSchedule = scheduleGroupService.updateScheduleGroup(
                userId, new ScheduleDto(scheduleEntity), scheduleDto.getGroupDto().stream().toList()
        );

        deleteCurrentOnlyRepeatSchedule(scheduleEntity);

        ScheduleDto updateDto = this.update(scheduleId, scheduleDto.getScheduleDto(), true);
        List<ScheduleNotificationDto> resultNotification = scheduleNotificationService.update(userId, scheduleId, scheduleDto.getNotificationDto().stream().toList());

        return new ScheduleDto.Response(updateDto, resultNotification, null, groupSchedule);
    }

    @Transactional
    public void deleteById(long scheduleId, long calendarId, long userId) {
        Optional<ScheduleEntity> schedule = scheduleRepository.findById(scheduleId);

        if (schedule.isEmpty()) {
            return;
        }

        dispatchAuthCheck(CRUDAction.DELETE, userId, schedule.get().getUserId(), schedule.get().getId(), calendarId);

        if (schedule.get().getRepeatId() != null) {
            throw new CustomException("has repeat-id");
        }

        CalendarEntity calendar = calendarRepository.findById(calendarId).orElseThrow(
                () -> new EntityNotFoundException("캘린더가 존재하지 않습니다.")
        );

        calendarServiceMap.get(calendar.getCategory()).checkAuthority(CRUDAction.DELETE, userId, userId, calendarId);

        try {
            scheduleNotificationRepository.deleteByScheduleId(scheduleId);
            scheduleGroupService.deleteScheduleGroup(userId, schedule.get().getUserId(), scheduleId);
            scheduleRepository.deleteById(scheduleId);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Schedule not found with schedule-id: " + scheduleId);
        }
    }

    @Transactional
    public void deleteCurrentOnlyRepeatSchedule(long scheduleId) {
        Optional<ScheduleEntity> standardSchedule = scheduleRepository.findById(scheduleId);
        if(standardSchedule.isEmpty()) {
            return;
        }

        if (standardSchedule.get().getRepeatId() != null) {
            //자신을 제외한 반복 일정이 없다면 반복 일정을 삭제해준다.
            if (scheduleRepository.countByRepeatId(standardSchedule.get().getRepeatId()) <= 1) {
                scheduleRepeatRepository.deleteById(standardSchedule.get().getRepeatId());
            }
        }
    }

    @Transactional
    public void deleteCurrentOnlyRepeatSchedule(ScheduleEntity standardSchedule) {
        if (standardSchedule.getRepeatId() != null) {
            //자신을 제외한 반복 일정이 없다면 반복 일정을 삭제해준다.
            if (scheduleRepository.countByRepeatId(standardSchedule.getRepeatId()) <= 1) {
                scheduleRepeatRepository.deleteById(standardSchedule.getRepeatId());
            }
        }
    }

    //현재 일정 이후의 반복 일정들을 모두 삭제
    @Transactional
    public void deleteFutureRepeatSchedules(long scheduleId, long userId) {
        //수정된 일정이 들어오니 수정전 일정으로 비교
        ScheduleEntity standardSchedule = scheduleRepository.findByIdAndUserId(scheduleId, userId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with schedule-id: " + scheduleId)
        );

        if (standardSchedule.getRepeatId() == null) {
            throw new EntityNotFoundException("repeat-id not found");
        }

        //반복되는 이후 일정들 가져오기 (기준 일정 제외)
        List<ScheduleEntity> scheduleList = scheduleRepository.findFutureRepeatSchedules(standardSchedule.getRepeatId(), standardSchedule.getStartAt(), userId);

        //반복 일정의 알림, 그룹 일정 삭제
        scheduleList.forEach( scheduleEntity -> {
            scheduleNotificationRepository.deleteByScheduleId(scheduleEntity.getId());
            scheduleGroupService.deleteScheduleGroup(userId, scheduleEntity.getUserId(), scheduleEntity.getId());
        });

        scheduleRepository.deleteAll(scheduleList);

        if (scheduleRepository.countByRepeatId(standardSchedule.getRepeatId()) <= 1) {
            scheduleRepeatRepository.deleteById(standardSchedule.getRepeatId());
        }
    }

    public Optional<ScheduleDto> findById(long scheduleId, long userId) {
        Optional<ScheduleEntity> findEntity = scheduleRepository.findByIdAndUserId(scheduleId, userId);

        return findEntity.map(ScheduleDto::new);
    }

    public List<ScheduleDto> findAll() {
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findAll());
    }

    public List<ScheduleDto> findByUserId(long userId) {
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findByUserId(userId));
    }

    public boolean existsById(long scheduleId) {
        return scheduleRepository.existsById(scheduleId);
    }

    public List<ScheduleDto> getSchedulesByDateRange(LocalDateTime startDate, LocalDateTime endDate, long userId) {
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findSchedules(startDate, endDate, userId));
    }

    @Transactional
    public Map<Long, List<ScheduleDto>> getScheduleByDateRangeAndCalendarId(LocalDateTime startDate, LocalDateTime endDate, long userId) {
        Map<Long, List<ScheduleDto>> result = new HashMap<>();

        for (long calendarId : CalendarUtility.getUserAllCalendar(userId, calendarServiceMap.values().stream().toList())) {
            List<ScheduleDto> scheduleDtos = ScheduleDto.fromScheduleEntityList(
                    scheduleRepository.findSchedulesByCalendarId(startDate, endDate, calendarId)
            );

            result.put(calendarId, scheduleDtos);
        }

        return result;
    }

    @Transactional
    public CursorPage<ScheduleDto> getNextSchedules(long userId, LocalDateTime start, LocalDateTime end, LocalDateTime cursorTime, long cursorId, int pageSize) {
        List<Long> calendars = CalendarUtility.getUserAllCalendar(userId, calendarServiceMap.values().stream().toList());

        Pageable pageable = PageRequest.of(0, pageSize);
        List<ScheduleEntity> events = scheduleQueryRepository.findSchedulesByCalendarIdAndUser(userId, calendars, start, end, cursorTime, cursorId, pageable);

        List<ScheduleDto> dtos = ScheduleDto.fromScheduleEntityList(events);

        String nextCursor = dtos.isEmpty() ? null : dtos.get(dtos.size() - 1).getStartAt().toString();

        return new CursorPage<>(dtos, nextCursor);
    }

    @Transactional
    public Map<Long, List<ScheduleDto>> getScheduleByDateRangeAndCalendarId(LocalDateTime startDate, LocalDateTime endDate, long userId, List<Long> calendarIdList) {

        Map<Long, List<ScheduleDto>> result = new HashMap<>();

        for (long calendarId : CalendarUtility.getAuthorizedCalendars(userId, calendarIdList, calendarServiceMap.values().stream().toList())) {
            List<ScheduleDto> scheduleDtos = ScheduleDto.fromScheduleEntityList(
                    scheduleRepository.findSchedulesByCalendarId(startDate, endDate, calendarId)
            );

            result.put(calendarId, scheduleDtos);
        }

        return result;
    }

    @Transactional
    public void dispatchAuthCheck(CRUDAction action, long userId, long createdUserId, Long scheduleId, ScheduleDto.Request requestDto) {
        ScheduleDto scheduleDto = requestDto.getScheduleDto();
        Set<ScheduleGroupDto> scheduleGroupDto = requestDto.getGroupDto();
        CalendarCategory calendarCategory = requestDto.getCalendarCategory();

        switch (action) {
            case CREATE -> {
                if (scheduleGroupDto.isEmpty()) {
                    calendarServiceMap.get(calendarCategory).checkAuthority(action, userId, userId, scheduleDto.getCalendarId());
                } else {
                    scheduleGroupService.checkScheduleGroupAuth(action, userId, createdUserId, scheduleId);
                }
            }
            case READ -> {

            }
            case UPDATE -> {
                boolean hasGroup = scheduleGroupService.getScheduleGroupUserCount(scheduleId) > 0;

                if (scheduleGroupDto.isEmpty()) {
                    if (hasGroup) {
                        scheduleGroupService.checkScheduleGroupAuth(CRUDAction.DELETE, userId, createdUserId, scheduleId);
                    } else {
                        calendarServiceMap.get(calendarCategory).checkAuthority(action, userId, userId, scheduleDto.getCalendarId());
                    }
                } else {
                    if (hasGroup) {
                        scheduleGroupService.checkScheduleGroupAuth(CRUDAction.UPDATE, userId, createdUserId, scheduleId);
                    } else {
                        scheduleGroupService.checkScheduleGroupAuth(CRUDAction.CREATE, userId, createdUserId, scheduleId);
                    }
                }
            }
            case DELETE -> {
                boolean hasGroup = scheduleGroupService.getScheduleGroupUserCount(scheduleId) > 0;

                if (hasGroup) {
                    scheduleGroupService.checkScheduleGroupAuth(action, userId, createdUserId, scheduleId);
                } else {
                    calendarServiceMap.get(calendarCategory).checkAuthority(action, userId, userId, scheduleDto.getCalendarId());
                }
            }
        }
    }

    public void dispatchAuthCheck(CRUDAction action, long userId, long createdUserId, Long scheduleId, long calendarId) {
        ScheduleDto.Request requestDto = new ScheduleDto.Request();
        CalendarEntity calendar = calendarRepository.findById(calendarId).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 캘린더입니다.")
        );

        requestDto.setScheduleDto(new ScheduleDto());
        requestDto.getScheduleDto().setCalendarId(calendar.getId());
        requestDto.setCalendarCategory(calendar.getCategory());

        dispatchAuthCheck(action, userId, createdUserId, scheduleId, requestDto);
    }
}
