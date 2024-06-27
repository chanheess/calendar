package com.chpark.calendar.repository;

import com.chpark.calendar.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<ScheduleEntity, Integer> {
    List<ScheduleEntity> findByTitleContaining(String title);
}
