package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.repository.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository repository) {
        this.scheduleRepository = repository;
    }

    public ScheduleDto create(ScheduleDto scheduleDto) {
        ScheduleEntity scheduleEntity = new ScheduleEntity(scheduleDto);
        ScheduleEntity savedEntity = scheduleRepository.save(scheduleEntity);

        return scheduleDto;
    }

    public List<ScheduleDto> findSchedulesByTitle(String title) {
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findByTitleContaining(title));
    }

    public Optional<ScheduleDto> update(int id, ScheduleDto scheduleDto) {

        Optional<ScheduleEntity> updateData = scheduleRepository.findById(id);
        if(updateData.isPresent()){
            ScheduleEntity schedule = updateData.get();
            schedule.setTitle(scheduleDto.getTitle());
            schedule.setDescription(scheduleDto.getDescription());
            schedule.setStartAt(scheduleDto.getStartAt());
            schedule.setEndAt(scheduleDto.getEndAt());

            ScheduleDto resultDto = new ScheduleDto(scheduleRepository.save(schedule));

            return Optional.of(resultDto);
        } else {
            return Optional.empty();
        }
    }

    public void deleteById(int id) {
        scheduleRepository.deleteById(id);
    }

    public List<ScheduleDto> findAll() {
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findAll());
    }

    //TODO: year, month와 date의 통합할 방법은 없는가
    public List<ScheduleDto> getSchedulesForYear(int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = startOfYear.plusYears(1).minusSeconds(1);
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findSchedules(startOfYear, endOfYear));
    }

    public List<ScheduleDto> getSchedulesForMonth(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findSchedules(startOfMonth, endOfMonth));
    }

    public List<ScheduleDto> getSchedulesForDate(int year, int month, int day) {
        LocalDateTime startOfDay = LocalDateTime.of(year, month, day, 0, 0);
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        return ScheduleDto.fromScheduleEntityList(scheduleRepository.findSchedules(startOfDay, endOfDay));
    }



}
