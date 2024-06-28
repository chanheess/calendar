package com.chpark.calendar.service;

import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.repository.CalendarRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public CalendarService(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    public int create(ScheduleEntity scheduleEntity) {
        calendarRepository.save(scheduleEntity);
        return scheduleEntity.getId();
    }

    public List<ScheduleEntity> findSchedulesByTitle(String title) {
        return calendarRepository.findByTitleContaining(title);
    }

    public void update(ScheduleEntity scheduleEntity) {

        Optional<ScheduleEntity> updateData = calendarRepository.findById(scheduleEntity.getId());
        if(updateData.isPresent()){
            ScheduleEntity schedule = updateData.get();
            schedule.setTitle(scheduleEntity.getTitle());
            schedule.setDescription(scheduleEntity.getDescription());
            schedule.setStartTime(scheduleEntity.getStartTime());
            schedule.setEndTime(scheduleEntity.getEndTime());

            calendarRepository.save(schedule);
        }
        else {
            throw new RuntimeException("Schedule not found with id " + scheduleEntity.getId());
        }
    }

    public void delete(int id) {
        Optional<ScheduleEntity> deleteEntity = calendarRepository.findById(id);
        if(deleteEntity.isPresent()) {
            calendarRepository.deleteById(id);
        }
        else {
            throw new RuntimeException("Schedule not found with id " + id);
        }
    }

}
