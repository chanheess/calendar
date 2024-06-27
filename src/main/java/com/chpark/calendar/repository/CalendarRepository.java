package com.chpark.calendar.repository;

import com.chpark.calendar.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarRepository extends JpaRepository<ScheduleEntity, Integer> {

}
