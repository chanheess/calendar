package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.dto.ScheduleRepeatDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.repository.ScheduleNotificationRepository;
import com.chpark.calendar.repository.ScheduleRepeatRepository;
import com.chpark.calendar.repository.ScheduleRepository;
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

    public Optional<ScheduleDto> create(ScheduleDto scheduleDto) {
        ScheduleEntity savedEntity = scheduleRepository.save(new ScheduleEntity(scheduleDto));

        return Optional.of(new ScheduleDto(savedEntity));
    }

    public List<ScheduleDto> findSchedulesByTitle(String title) {
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findByTitleContaining(title));
    }

    public ScheduleDto update(int id, ScheduleDto scheduleDto) {
        ScheduleEntity schedule = scheduleRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with id: " + id)
        );

        schedule.setTitle(scheduleDto.getTitle());
        schedule.setDescription(scheduleDto.getDescription());
        schedule.setStartAt(scheduleDto.getStartAt());
        schedule.setEndAt(scheduleDto.getEndAt());
        schedule.setRepeatId(null);

        return new ScheduleDto(scheduleRepository.save(schedule));
    }

    @Transactional
    public ScheduleDto.repeatResponse repeatCurrentOnlyScheduleUpdate(int scheduleId, ScheduleDto scheduleDto) {

        //수정된 일정이 들어오니 수정전 일정으로 비교
        ScheduleEntity standardSchedule = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with id: " + scheduleId)
        );

        if(standardSchedule.getRepeatId() == null) {
            throw new EntityNotFoundException("repeat_id not found");
        }

        //반복되는 이후 일정들 가져오기 (기준 일정 제외)
        List<ScheduleEntity> scheduleList = scheduleRepository.findFutureRepeatSchedules(standardSchedule.getRepeatId(), standardSchedule.getStartAt());

        //자신을 제외한 반복되는 일정이 없다면 반복 일정을 삭제해준다.
        if(scheduleList.isEmpty()) {
            scheduleRepeatRepository.deleteById(standardSchedule.getRepeatId());
        }

        //일정 내용 update
        ScheduleDto resultSchedule = this.update(scheduleId, scheduleDto);

        return new ScheduleDto.repeatResponse(resultSchedule);
    }

    @Transactional
    public ScheduleDto.repeatResponse repeatCurrentAndFutureScheduleUpdate(int scheduleId, ScheduleDto.repeatRequest scheduleDto) {

        //TODO: 알림의 내용도 바뀌어서 들어왔을 때는 가정하지 않았다. 추후에 추가하자.

        //수정된 일정이 들어오니 수정전 일정으로 비교
        ScheduleEntity standardSchedule = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with id: " + scheduleId)
        );

        if(standardSchedule.getRepeatId() == null) {
            throw new EntityNotFoundException("repeat_id not found");
        }

        //반복되는 이후 일정들 가져오기 (기준 일정 제외)
        List<ScheduleEntity> scheduleList = scheduleRepository.findFutureRepeatSchedules(standardSchedule.getRepeatId(), standardSchedule.getStartAt());

        //반복 일정의 알림들 삭제
        scheduleList.forEach( scheduleEntity -> {
            scheduleNotificationRepository.deleteByScheduleId(scheduleEntity.getId());
        });

        //반복 일정들 삭제
        scheduleRepository.deleteAll(scheduleList);

        //기준 일정이 첫날이면 일정 반복을 삭제해준다.
        if(scheduleRepository.existsByPreviousRepeatedSchedule(standardSchedule.getRepeatId(), standardSchedule.getStartAt())) {
            scheduleRepeatRepository.deleteById(standardSchedule.getRepeatId());
        }

        //일정 내용 update
        ScheduleDto resultSchedule = this.update(scheduleId, scheduleDto.getScheduleDto());

        //일정 반복 생성
        ScheduleRepeatDto.Response resultScheduleRepeat = scheduleRepeatService.create(scheduleId, scheduleDto.getRepeatDto());

        return new ScheduleDto.repeatResponse(resultSchedule, resultScheduleRepeat);
    }

    @Transactional
    public void deleteById(int id) {
        try {
            scheduleNotificationRepository.deleteByScheduleId(id);
            scheduleRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Schedule not found with id: " + id);
        }
    }

    @Transactional
    public void deleteCurrentOnlyRepeatSchedule(int id) {

        //수정된 일정이 들어오니 수정전 일정으로 비교
        ScheduleEntity standardSchedule = scheduleRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with id: " + id)
        );

        if(standardSchedule.getRepeatId() == null) {
            throw new EntityNotFoundException("repeat_id not found");
        }

        //반복되는 이후 일정들 가져오기 (기준 일정 제외)
        List<ScheduleEntity> scheduleList = scheduleRepository.findFutureRepeatSchedules(standardSchedule.getRepeatId(), standardSchedule.getStartAt());

        //자신을 제외한 반복되는 일정이 없다면 반복 일정을 삭제해준다.
        if(scheduleList.isEmpty()) {
            scheduleRepeatRepository.deleteById(standardSchedule.getRepeatId());
        }

        this.deleteById(id);
    }

    @Transactional
    public void deleteCurrentAndFutureRepeatSchedule(int id) {

        //수정된 일정이 들어오니 수정전 일정으로 비교
        ScheduleEntity standardSchedule = scheduleRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with id: " + id)
        );

        if(standardSchedule.getRepeatId() == null) {
            throw new EntityNotFoundException("repeat_id not found");
        }

        //반복되는 이후 일정들 가져오기 (기준 일정 제외)
        List<ScheduleEntity> scheduleList = scheduleRepository.findFutureRepeatSchedules(standardSchedule.getRepeatId(), standardSchedule.getStartAt());

        //반복 일정의 알림들 삭제
        scheduleList.forEach( scheduleEntity -> {
            scheduleNotificationRepository.deleteByScheduleId(scheduleEntity.getId());
        });

        //반복 일정들 삭제
        scheduleRepository.deleteAll(scheduleList);

        //기준 일정이 첫날이면 일정 반복을 삭제해준다.
        if(scheduleRepository.existsByPreviousRepeatedSchedule(standardSchedule.getRepeatId(), standardSchedule.getStartAt())) {
            scheduleRepeatRepository.deleteById(standardSchedule.getRepeatId());
        }

        this.deleteById(id);
    }

    public ScheduleDto findById(int id) {
        ScheduleEntity findEntity = scheduleRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Schedule not found with id: " + id)
        );

        return new ScheduleDto(findEntity);
    }

    public List<ScheduleDto> findAll() {
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findAll());
    }

    public boolean existsById(int id) {
        return scheduleRepository.existsById(id);
    }

    public List<ScheduleDto> getSchedulesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findSchedules(startDate, endDate));
    }

}
