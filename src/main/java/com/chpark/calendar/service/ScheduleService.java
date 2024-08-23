package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.dto.ScheduleNotificationDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final ScheduleRepeatRepository scheduleRepeatRepository;

    private final ScheduleRepeatService scheduleRepeatService;
    private final ScheduleNotificationService scheduleNotificationService;

    public ScheduleDto create(ScheduleDto scheduleDto) {
        //빈 제목일 경우 제목 없음으로 처리
        if(Objects.equals(scheduleDto.getTitle(), "")) {
            scheduleDto.setTitle("Untitled");
        }

        ScheduleEntity savedEntity = scheduleRepository.save(new ScheduleEntity(scheduleDto));

        return new ScheduleDto(savedEntity);
    }

    @Transactional
    public ScheduleDto.Response createByForm(ScheduleDto.Request scheduleDto) {

        ScheduleDto resultSchedule = this.create(scheduleDto.getScheduleDto());

        List<ScheduleNotificationDto.Response> resultNotifications = scheduleNotificationService.create(resultSchedule.getId(), scheduleDto.getNotificationDto());

        ScheduleRepeatDto.Response resultRepeat = null;

        if(scheduleDto.getRepeatDto() != null) {
            resultRepeat = scheduleRepeatService.create(resultSchedule.getId(), scheduleDto.getRepeatDto());
            resultSchedule.setRepeatId(resultRepeat.getId());
        }

        return new ScheduleDto.Response(resultSchedule, resultNotifications, resultRepeat);
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
    public ScheduleDto.Response repeatCurrentOnlyScheduleUpdate(int scheduleId, ScheduleDto scheduleDto) {

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

        return new ScheduleDto.Response(resultSchedule);
    }

    @Transactional
    public ScheduleDto.Response repeatCurrentAndFutureScheduleUpdate(int scheduleId, ScheduleDto.Request scheduleDto) {



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

        //TODO: 알림의 내용도 바뀌어서 들어왔을 때는 가정하지 않았다. 추후에 추가하자.
        //알림 내용 수정
        List<ScheduleNotificationDto.Response> resultNotification = new ArrayList<>();

        //일정 반복 생성
        ScheduleRepeatDto.Response resultRepeat = scheduleRepeatService.create(scheduleId, scheduleDto.getRepeatDto());

        return new ScheduleDto.Response(resultSchedule, resultNotification, resultRepeat);
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

    public Optional<ScheduleDto> findById(int id) {
        Optional<ScheduleEntity> findEntity = scheduleRepository.findById(id);

        return findEntity.map(ScheduleDto::new);
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
