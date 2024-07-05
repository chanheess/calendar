package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.repository.CalendarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public CalendarService(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    public ScheduleDto create(ScheduleDto scheduleDto) {
        ScheduleEntity scheduleEntity = new ScheduleEntity(scheduleDto);
        ScheduleEntity savedEntity = calendarRepository.save(scheduleEntity);

        return scheduleDto;
    }

    public List<ScheduleDto> findSchedulesByTitle(String title) {
        return ScheduleDto.ConvertScheduleEntities(calendarRepository.findByTitleContaining(title));
    }

    public Optional<ScheduleDto> update(ScheduleDto scheduleDto) {

        Optional<ScheduleEntity> updateData = calendarRepository.findById(scheduleDto.getId());
        if(updateData.isPresent()){
            ScheduleEntity schedule = updateData.get();
            schedule.setTitle(scheduleDto.getTitle());
            schedule.setDescription(scheduleDto.getDescription());
            schedule.setStartAt(scheduleDto.getStartAt());
            schedule.setEndAt(scheduleDto.getEndAt());

            ScheduleDto resultDto = new ScheduleDto(calendarRepository.save(schedule));

            return Optional.of(resultDto);
        } else {
            return Optional.empty();
        }
    }

    public void delete(int id) {
        calendarRepository.deleteById(id);
    }

    public List<ScheduleDto> findAll() {
        return ScheduleDto.ConvertScheduleEntities(calendarRepository.findAll());
    }

    //TODO: year, month와 date의 통합할 방법은 없는가
    public List<ScheduleDto> getSchedulesForYear(int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = startOfYear.plusYears(1).minusSeconds(1);
        return ScheduleDto.ConvertScheduleEntities(calendarRepository.findSchedules(startOfYear, endOfYear));
    }

    public List<ScheduleDto> getSchedulesForMonth(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        return ScheduleDto.ConvertScheduleEntities(calendarRepository.findSchedules(startOfMonth, endOfMonth));
    }

    public List<ScheduleDto> getSchedulesForDate(int year, int month, int day) {
        LocalDateTime startOfDay = LocalDateTime.of(year, month, day, 0, 0);
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        return ScheduleDto.ConvertScheduleEntities(calendarRepository.findSchedules(startOfDay, endOfDay));
    }



}
