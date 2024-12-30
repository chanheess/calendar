package com.chpark.calendar.service.schedule;

import com.chpark.calendar.dto.schedule.ScheduleDto;
import com.chpark.calendar.dto.schedule.ScheduleNotificationDto;
import com.chpark.calendar.dto.schedule.ScheduleRepeatDto;
import com.chpark.calendar.entity.schedule.ScheduleEntity;
import com.chpark.calendar.entity.schedule.ScheduleRepeatEntity;
import com.chpark.calendar.exception.CustomException;
import com.chpark.calendar.repository.schedule.ScheduleNotificationRepository;
import com.chpark.calendar.repository.schedule.ScheduleRepeatRepository;
import com.chpark.calendar.repository.schedule.ScheduleRepository;
import com.chpark.calendar.utility.ScheduleUtility;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final ScheduleRepeatRepository scheduleRepeatRepository;

    private final ScheduleRepeatService scheduleRepeatService;
    private final ScheduleNotificationService scheduleNotificationService;

    @Transactional
    public ScheduleDto create(ScheduleDto scheduleDto, long userId) {
        this.validateScheduleDto(scheduleDto);
        //빈 제목일 경우 제목 없음으로 처리
        scheduleDto.setTitle(scheduleDto.getTitle().isEmpty() ? "Untitled" : scheduleDto.getTitle());

        ScheduleEntity savedEntity = new ScheduleEntity(scheduleDto);
        savedEntity.setUserId(userId);
        savedEntity = scheduleRepository.save(savedEntity);

        return new ScheduleDto(savedEntity);
    }

    @Transactional
    public ScheduleDto.Response createByForm(ScheduleDto.Request scheduleDto, long userId) {
        ScheduleDto resultSchedule = this.create(scheduleDto.getScheduleDto(), userId);
        List<ScheduleNotificationDto> resultNotifications = scheduleNotificationService.create(resultSchedule.getId(), scheduleDto.getNotificationDto());
        ScheduleRepeatDto resultRepeat = null;

        if(scheduleDto.getRepeatDto() != null) {
            resultRepeat = scheduleRepeatService.create(resultSchedule.getId(), scheduleDto.getRepeatDto(), userId);
        }

        return new ScheduleDto.Response(resultSchedule, resultNotifications, resultRepeat);
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
        ScheduleEntity schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with schedule-id: " + scheduleId)
        );

        this.validateScheduleDto(scheduleDto);

        if (scheduleDto.getTitle() != null) {
            schedule.setTitle(scheduleDto.getTitle().isEmpty() ? "Untitled" : scheduleDto.getTitle());
        }
        if (scheduleDto.getDescription() != null) {
            schedule.setDescription(scheduleDto.getDescription());
        }
        if (scheduleDto.getStartAt() != null) {
            schedule.setStartAt(scheduleDto.getStartAt());
        }
        if (scheduleDto.getEndAt() != null) {
            schedule.setEndAt(scheduleDto.getEndAt());
        }
        //반복 수정시에 repeatId를 null로 비워준다.
        if (isRepeat) {
            schedule.setRepeatId(null);
        }

        return new ScheduleDto(scheduleRepository.save(schedule));
    }

    @Transactional
    public ScheduleDto.Response updateSchedule(long scheduleId, boolean isRepeatChecked, ScheduleDto.Request scheduleDto, long userId) {
        if(scheduleRepository.getRepeatId(scheduleId, userId).isPresent()) {
            throw new CustomException("has repeat-id");
        }

        ScheduleDto updateDto = this.update(scheduleId, scheduleDto.getScheduleDto(), userId);
        List<ScheduleNotificationDto> updateNotificationDto = scheduleNotificationService.update(scheduleId, scheduleDto.getNotificationDto());
        ScheduleRepeatDto updateRepeatDto = null;

        if(isRepeatChecked){
            updateRepeatDto = scheduleRepeatService.create(scheduleId, scheduleDto.getRepeatDto(), userId);
        }
        
        return new ScheduleDto.Response(updateDto, updateNotificationDto, updateRepeatDto);
    }

    @Transactional
    public ScheduleDto.Response updateRepeatSchedule(long scheduleId, boolean isRepeatChecked, ScheduleDto.Request scheduleDto, long userId) {
        Long repeatId = scheduleRepository.getRepeatId(scheduleId, userId).orElseThrow(
                () -> new EntityNotFoundException("not found repeat-id")
        );

        if(!isRepeatChecked) {
            this.deleteFutureRepeatSchedules(scheduleId, userId);

            ScheduleDto updateDto = this.update(scheduleId, scheduleDto.getScheduleDto(), true, userId);
            List<ScheduleNotificationDto> updateNotificationDto = scheduleNotificationService.update(scheduleId, scheduleDto.getNotificationDto());

            return new ScheduleDto.Response(updateDto, updateNotificationDto);
        }

        if(scheduleRepeatService.isModified(repeatId, scheduleDto.getRepeatDto())){
            //기존 반복 일정 삭제 -> 일정 및 알림 업데이트 -> 새로운 반복 일정 등록의 순서가 어긋나면 안 된다.

            //기존 반복 일정 삭제
            this.deleteFutureRepeatSchedules(scheduleId, userId);

            //일정 및 알림 업데이트
            ScheduleDto updateDto = this.update(scheduleId, scheduleDto.getScheduleDto(), true, userId);
            List<ScheduleNotificationDto> updateNotificationDto = scheduleNotificationService.update(scheduleId, scheduleDto.getNotificationDto());

            //새로운 반복 일정 등록
            ScheduleRepeatDto updateRepeatDto = scheduleRepeatService.create(scheduleId, scheduleDto.getRepeatDto(), userId);

            return new ScheduleDto.Response(updateDto, updateNotificationDto, updateRepeatDto);
        }

        return this.updateRepeatCurrentAndFutureSchedules(scheduleId, scheduleDto, userId);
    }

    @Transactional
    public ScheduleDto.Response updateRepeatCurrentOnlySchedule(long scheduleId, ScheduleDto.Request scheduleDto, long userId) {
        if(scheduleRepository.getRepeatId(scheduleId, userId).isEmpty()) {
            throw new EntityNotFoundException("not found repeat-id");
        }

        this.deleteCurrentOnlyRepeatSchedule(scheduleId, userId);

        ScheduleDto resultSchedule = this.update(scheduleId, scheduleDto.getScheduleDto(), true, userId);
        List<ScheduleNotificationDto> resultNotification = scheduleNotificationService.update(scheduleId, scheduleDto.getNotificationDto());

        return new ScheduleDto.Response(resultSchedule, resultNotification);
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
        List<ScheduleNotificationDto> resultNotification = scheduleNotificationService.update(scheduleId, scheduleDto.getNotificationDto());
        ScheduleRepeatDto resultRepeat = new ScheduleRepeatDto(scheduleRepeatEntity);

        return new ScheduleDto.Response(resultSchedule, resultNotification, resultRepeat);
    }

    @Transactional
    public void deleteById(long scheduleId, long userId) {
        if(scheduleRepository.getRepeatId(scheduleId, userId).isPresent()) {
            throw new CustomException("has repeat-id");
        }

        try {
            scheduleNotificationRepository.deleteByScheduleId(scheduleId);
            scheduleRepository.deleteByIdAndUserId(scheduleId, userId);
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

        if(standardSchedule.getRepeatId() != null) {
            //자신을 제외한 반복 일정이 없다면 반복 일정을 삭제해준다.
            if(scheduleRepository.isLastRemainingRepeatSchedule(standardSchedule.getRepeatId())) {
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

        if(standardSchedule.getRepeatId() == null) {
            throw new EntityNotFoundException("repeat-id not found");
        }

        //반복되는 이후 일정들 가져오기 (기준 일정 제외)
        List<ScheduleEntity> scheduleList = scheduleRepository.findFutureRepeatSchedules(standardSchedule.getRepeatId(), standardSchedule.getStartAt(), userId);

        //반복 일정의 알림들 삭제
        scheduleList.forEach( scheduleEntity -> {
            scheduleNotificationRepository.deleteByScheduleId(scheduleEntity.getId());
        });

        scheduleRepository.deleteAll(scheduleList);

        if(scheduleRepository.isLastRemainingRepeatSchedule(standardSchedule.getRepeatId())) {
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

    public void validateScheduleDto(ScheduleDto scheduleDto) {
        if(!scheduleDto.getStartAt().isBefore(scheduleDto.getEndAt())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
    }

}
