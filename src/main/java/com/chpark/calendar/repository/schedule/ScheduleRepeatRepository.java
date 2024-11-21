package com.chpark.calendar.repository.schedule;

import com.chpark.calendar.entity.ScheduleRepeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepeatRepository extends JpaRepository<ScheduleRepeatEntity, Integer> {

}
