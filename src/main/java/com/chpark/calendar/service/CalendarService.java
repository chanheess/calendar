package com.chpark.calendar.service;

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

    public ScheduleEntity create(ScheduleEntity scheduleEntity) {
        return calendarRepository.save(scheduleEntity);
    }

    public List<ScheduleEntity> findSchedulesByTitle(String title) {
        return calendarRepository.findByTitleContaining(title);
    }

    public ScheduleEntity update(ScheduleEntity scheduleEntity) {

        Optional<ScheduleEntity> updateData = calendarRepository.findById(scheduleEntity.getId());
        if(updateData.isPresent()){
            ScheduleEntity schedule = updateData.get();
            schedule.setTitle(scheduleEntity.getTitle());
            schedule.setDescription(scheduleEntity.getDescription());
            schedule.setStartAt(scheduleEntity.getStartAt());
            schedule.setEndAt(scheduleEntity.getEndAt());

            return calendarRepository.save(schedule);
        }
        else {
            throw new RuntimeException("Schedule not found with id " + scheduleEntity.getId());
        }
    }

    public void delete(int id) {
        calendarRepository.deleteById(id);
    }

    public List<ScheduleEntity> findAll() {
        return calendarRepository.findAll();
    }

    public List<ScheduleEntity> getSchedulesForMonth(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        return calendarRepository.findSchedules(startOfMonth, endOfMonth);
    }

    public List<ScheduleEntity> getSchedulesForDate(int year, int month, int day) {
        LocalDateTime startOfDay = LocalDateTime.of(year, month, day, 0, 0);
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        return calendarRepository.findSchedules(startOfDay, endOfDay);
    }

}
