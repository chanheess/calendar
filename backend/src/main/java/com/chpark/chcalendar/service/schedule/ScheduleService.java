package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.CursorPage;
import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.dto.schedule.ScheduleGroupDto;
import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.chpark.chcalendar.dto.schedule.ScheduleRepeatDto;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleRepeatEntity;
import com.chpark.chcalendar.exception.CustomException;
import com.chpark.chcalendar.exception.ScheduleException;
import com.chpark.chcalendar.repository.schedule.ScheduleNotificationRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepeatRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import com.chpark.chcalendar.service.user.GroupUserService;
import com.chpark.chcalendar.utility.CalendarUtility;
import com.chpark.chcalendar.utility.ScheduleUtility;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final ScheduleRepeatRepository scheduleRepeatRepository;

    private final ScheduleRepeatService scheduleRepeatService;
    private final ScheduleNotificationService scheduleNotificationService;
    private final ScheduleGroupService scheduleGroupService;

    private final GroupUserService groupUserService;
    private final UserCalendarService userCalendarService;

    @Transactional
    public ScheduleDto create(ScheduleDto scheduleDto, long userId) {
        this.validateScheduleDto(scheduleDto);
        CalendarUtility.checkCalendarAuthority(userId, userId, scheduleDto.getCalendarId(), null, groupUserService, userCalendarService, scheduleGroupService);

        //빈 제목일 경우 제목 없음으로 처리
        scheduleDto.setTitle(scheduleDto.getTitle().isEmpty() ? "Untitled" : scheduleDto.getTitle());
        scheduleDto.setUserId(userId);

        ScheduleEntity savedEntity = new ScheduleEntity(scheduleDto);
        savedEntity = scheduleRepository.save(savedEntity);

        return new ScheduleDto(savedEntity);
    }

    @Transactional
    public ScheduleDto.Response createByForm(ScheduleDto.Request scheduleDto, long userId) {
        ScheduleDto resultSchedule = this.create(scheduleDto.getScheduleDto(), userId);
        List<ScheduleNotificationDto> resultNotifications = scheduleNotificationService.create(userId, resultSchedule.getId(), scheduleDto.getNotificationDto().stream().toList());
        ScheduleRepeatDto resultRepeat = scheduleRepeatService.create(resultSchedule.getId(), scheduleDto.getRepeatDto(), userId);
        List<ScheduleGroupDto> groupSchedule = scheduleGroupService.createScheduleGroup(resultSchedule, scheduleDto.getGroupDto());

        return new ScheduleDto.Response(resultSchedule, resultNotifications, resultRepeat, groupSchedule);
    }

    @Transactional
    public List<ScheduleDto> findSchedulesByTitle(String title, long userId) {
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findByTitleContainingAndUserId(title, userId));
    }

    @Transactional
    public ScheduleDto update(long scheduleId, ScheduleDto scheduleDto, long userId) {
        return this.update(scheduleId, scheduleDto, false, userId);
    }

    @Transactional
    public ScheduleDto update(long scheduleId, ScheduleDto scheduleDto, boolean isRepeat, long userId) {
        ScheduleEntity schedule = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found")
        );

        CalendarUtility.checkCalendarAuthority(userId, schedule.getUserId(), schedule.getCalendarId(), schedule.getId(), groupUserService, userCalendarService, scheduleGroupService);

        if (scheduleDto.getTitle() != null) {
            schedule.setTitle(scheduleDto.getTitle().isEmpty() ? "Untitled" : scheduleDto.getTitle());
        }
        if (scheduleDto.getDescription() != null) {
            schedule.setDescription(scheduleDto.getDescription());
        }
        if (scheduleDto.getStartAt() != null) {
            this.validateScheduleDto(scheduleDto);
            schedule.setStartAt(scheduleDto.getStartAt());
        }
        if (scheduleDto.getEndAt() != null) {
            this.validateScheduleDto(scheduleDto);
            schedule.setEndAt(scheduleDto.getEndAt());
        }
        //반복 수정시에 repeatId를 null로 비워준다.
        if (isRepeat) {
            schedule.setRepeatId(null);
        }

        this.validateScheduleDto(scheduleDto);
        schedule.setCalendarId(scheduleDto.getCalendarId());

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

        ScheduleDto updateDto = this.update(scheduleId, scheduleDto.getScheduleDto(), userId);
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

        //반복 일정 삭제
        if(!isRepeatChecked) {
            this.deleteFutureRepeatSchedules(scheduleId, userId);

            ScheduleDto updateDto = this.update(scheduleId, scheduleDto.getScheduleDto(), true, userId);
            List<ScheduleNotificationDto> updateNotificationDto = scheduleNotificationService.update(userId, scheduleId, scheduleDto.getNotificationDto().stream().toList());
            List<ScheduleGroupDto> groupSchedule = scheduleDto.getGroupDto().stream().toList();

            return new ScheduleDto.Response(updateDto, updateNotificationDto, groupSchedule);
        }

        //반복 일정의 조건이 수정 되었을 때
        if (scheduleRepeatService.isModified(scheduleEntity.getRepeatId(), scheduleDto.getRepeatDto())){
            //기존 반복 일정 삭제 -> 일정 및 알림 업데이트 -> 새로운 반복 일정 등록의 순서가 어긋나면 안 된다.

            //기존 반복 일정 삭제
            this.deleteFutureRepeatSchedules(scheduleId, userId);

            //일정 및 알림 업데이트
            ScheduleDto updateDto = this.update(scheduleId, scheduleDto.getScheduleDto(), true, userId);
            List<ScheduleNotificationDto> updateNotificationDto = scheduleNotificationService.update(userId, scheduleId, scheduleDto.getNotificationDto().stream().toList());

            //새로운 반복 일정 등록
            ScheduleRepeatDto updateRepeatDto = scheduleRepeatService.create(scheduleId, scheduleDto.getRepeatDto(), userId);

            //그룹 일정 설정
            List<ScheduleGroupDto> groupSchedule = scheduleGroupService.updateScheduleGroup(userId, updateDto, scheduleDto.getGroupDto().stream().toList());

            if (!scheduleRepeatService.isMasterSchedule(scheduleEntity.getRepeatId(), scheduleId)) {
                groupSchedule = scheduleGroupService.createScheduleGroup(updateDto, scheduleDto.getGroupDto());
            }

            return new ScheduleDto.Response(updateDto, updateNotificationDto, updateRepeatDto, groupSchedule);
        }

        //일정의 내용만 수정 되었을 때
        return this.updateRepeatCurrentAndFutureSchedules(scheduleId, scheduleDto, userId);
    }

    @Transactional
    public ScheduleDto.Response updateRepeatCurrentOnlySchedule(long scheduleId, ScheduleDto.Request scheduleDto, long userId) {
        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ScheduleException("Not found schedule")
        );

        if (scheduleEntity.getRepeatId() == null) {
            throw new ScheduleException("Not repeat schedule");
        }

        this.deleteCurrentOnlyRepeatSchedule(scheduleId, userId);

        ScheduleDto updateDto = this.update(scheduleId, scheduleDto.getScheduleDto(), true, userId);
        List<ScheduleNotificationDto> resultNotification = scheduleNotificationService.update(userId, scheduleId, scheduleDto.getNotificationDto().stream().toList());
        List<ScheduleGroupDto> groupSchedule = scheduleGroupService.updateScheduleGroup(userId, updateDto, scheduleDto.getGroupDto().stream().toList());

        if (!scheduleRepeatService.isMasterSchedule(scheduleEntity.getRepeatId(), scheduleId)) {
            groupSchedule = scheduleGroupService.createScheduleGroup(updateDto, scheduleDto.getGroupDto());
        }

        return new ScheduleDto.Response(updateDto, resultNotification, groupSchedule);
    }

    @Transactional
    public ScheduleDto.Response updateRepeatCurrentAndFutureSchedules(long scheduleId, ScheduleDto.Request scheduleDto, long userId) {
        //수정된 일정이 들어오니 수정전 일정으로 비교
        ScheduleEntity standardSchedule = scheduleRepository.findByIdAndUserId(scheduleId, userId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with schedule-id: " + scheduleId)
        );

        if(standardSchedule.getRepeatId() == null) {
            throw new EntityNotFoundException("repeat-id not found");
        }

        ScheduleRepeatEntity scheduleRepeatEntity = scheduleRepeatRepository.findById(standardSchedule.getRepeatId()).orElseThrow(
                () -> new EntityNotFoundException("repeat not found")
        );

        List<ScheduleEntity> schedules = scheduleRepository.findCurrentAndFutureRepeatSchedules(scheduleRepeatEntity.getId(), standardSchedule.getStartAt(), userId);

        if(!schedules.isEmpty()) {
            for(int i = 0; i < schedules.size(); i++) {
                if (scheduleDto.getScheduleDto().getTitle() != null) {
                    schedules.get(i).setTitle(scheduleDto.getScheduleDto().getTitle().isEmpty() ? "Untitled" : scheduleDto.getScheduleDto().getTitle());
                }
                if(scheduleDto.getScheduleDto().getDescription() != null) {
                    schedules.get(i).setDescription(scheduleDto.getScheduleDto().getDescription());
                }
                if (scheduleDto.getScheduleDto().getStartAt() != null) {
                    schedules.get(i).setStartAt(ScheduleUtility.calculateRepeatPlusDate(
                            scheduleDto.getScheduleDto().getStartAt(), scheduleRepeatEntity.getRepeatType(), scheduleRepeatEntity.getRepeatInterval() * i));
                }
                if (scheduleDto.getScheduleDto().getEndAt() != null) {
                    schedules.get(i).setEndAt(ScheduleUtility.calculateRepeatPlusDate(
                            scheduleDto.getScheduleDto().getEndAt(), scheduleRepeatEntity.getRepeatType(), scheduleRepeatEntity.getRepeatInterval() * i));
                }
            }
            scheduleRepository.saveAll(schedules);
        }

        ScheduleDto resultSchedule = new ScheduleDto(standardSchedule);
        List<ScheduleNotificationDto> resultNotification = scheduleNotificationService.update(userId, scheduleId, scheduleDto.getNotificationDto().stream().toList());
        ScheduleRepeatDto resultRepeat = new ScheduleRepeatDto(scheduleRepeatEntity);
        List<ScheduleGroupDto> groupSchedule = scheduleDto.getGroupDto().stream().toList();

        return new ScheduleDto.Response(resultSchedule, resultNotification, resultRepeat, groupSchedule);
    }

    @Transactional
    public void deleteById(long scheduleId, long calendarId, long userId) {
        Optional<ScheduleEntity> schedule = scheduleRepository.findById(scheduleId);

        if (schedule.isEmpty()) {
            return;
        }

        if (schedule.get().getRepeatId() != null) {
            throw new CustomException("has repeat-id");
        }

        //그룹와 캘린더에 속했는지 확인
        CalendarUtility.checkCalendarAuthority(userId, schedule.get().getUserId(), calendarId, schedule.get().getId(), groupUserService, userCalendarService, scheduleGroupService);

        try {
            scheduleNotificationRepository.deleteByScheduleId(scheduleId);
            scheduleRepository.deleteById(scheduleId);
            scheduleGroupService.deleteScheduleNotification(scheduleId);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Schedule not found with schedule-id: " + scheduleId);
        }
    }

    @Transactional
    public void deleteCurrentOnlyRepeatSchedule(long scheduleId, long userId) {
        //수정된 일정이 들어오니 수정전 일정으로 비교
        ScheduleEntity standardSchedule = scheduleRepository.findByIdAndUserId(scheduleId, userId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with schedule-id: " + scheduleId)
        );

        if (standardSchedule.getRepeatId() != null) {
            //자신을 제외한 반복 일정이 없다면 반복 일정을 삭제해준다.
            if (scheduleRepository.isLastRemainingRepeatSchedule(standardSchedule.getRepeatId())) {
                scheduleRepeatRepository.deleteById(standardSchedule.getRepeatId());
                scheduleGroupService.deleteScheduleGroup(scheduleId);
                return;
            }

            //자신이 master 일정이라면 다음 일정이 master 일정을 하도록 변경한다.
            changeRepeatMasterScheduleId(userId, standardSchedule);
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

        //반복 일정의 알림들 삭제
        scheduleList.forEach( scheduleEntity -> {
            scheduleNotificationRepository.deleteByScheduleId(scheduleEntity.getId());
        });

        scheduleRepository.deleteAll(scheduleList);

        if (scheduleRepository.isLastRemainingRepeatSchedule(standardSchedule.getRepeatId())) {
            scheduleRepeatRepository.deleteById(standardSchedule.getRepeatId());
            scheduleGroupService.deleteScheduleGroup(scheduleId);
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

        for (long calendarId : CalendarUtility.getUserCalendars(userId, groupUserService, userCalendarService)) {
            List<ScheduleDto> scheduleDtos = ScheduleDto.fromScheduleEntityList(
                    scheduleRepository.findSchedulesByCalendarId(startDate, endDate, calendarId)
            );

            result.put(calendarId, scheduleDtos);
        }

        return result;
    }

    @Transactional
    public CursorPage<ScheduleDto> getNextSchedules(long userId, LocalDateTime start, LocalDateTime end, LocalDateTime cursorTime, long cursorId, int pageSize) {
        List<Long> calendars = CalendarUtility.getUserCalendars(userId, groupUserService, userCalendarService);

        Pageable pageable = PageRequest.of(0, pageSize);
        List<ScheduleEntity> events = scheduleRepository.findSchedulesByCalendarIdAndUser(userId, calendars, start, end, cursorTime, cursorId, pageable);

        List<ScheduleDto> dtos = events.stream()
                .map(ScheduleDto::fromScheduleEntity)
                .collect(Collectors.toList());

        String nextCursor = dtos.isEmpty() ? null : dtos.get(dtos.size() - 1).getStartAt().toString();

        return new CursorPage<>(dtos, nextCursor);
    }

    @Transactional
    public Map<Long, List<ScheduleDto>> getScheduleByDateRangeAndCalendarId(LocalDateTime startDate, LocalDateTime endDate, long userId, List<Long> calendarIdList) {

        Map<Long, List<ScheduleDto>> result = new HashMap<>();

        for (long calendarId : CalendarUtility.getAuthorizedCalendars(userId, calendarIdList, groupUserService, userCalendarService)) {
            List<ScheduleDto> scheduleDtos = ScheduleDto.fromScheduleEntityList(
                    scheduleRepository.findSchedulesByCalendarId(startDate, endDate, calendarId)
            );

            result.put(calendarId, scheduleDtos);
        }

        return result;
    }

    public void validateScheduleDto(ScheduleDto scheduleDto) {
        if(!scheduleDto.getStartAt().isBefore(scheduleDto.getEndAt())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
    }

    @Transactional
    public void changeRepeatMasterScheduleId(long userId, ScheduleEntity scheduleEntity) {
        ScheduleRepeatEntity repeatEntity = scheduleRepeatRepository.findById(scheduleEntity.getRepeatId()).orElseThrow(
                () -> new EntityNotFoundException("Repeat not found")
        );

        if (repeatEntity.getMasterScheduleId() == scheduleEntity.getId()) {
            //master 일정일 경우에 master schedule을 다음 일정에 이관 해준다.
            List<ScheduleEntity> scheduleList = scheduleRepository.findFutureRepeatSchedules(scheduleEntity.getRepeatId(), scheduleEntity.getStartAt(), userId);

            for (ScheduleEntity schedule : scheduleList) {
                repeatEntity.setMasterScheduleId(schedule.getId());
                scheduleRepeatRepository.save(repeatEntity);
                break;
            }
        }
    }

}
