package com.chpark.calendar.repository;

import com.chpark.calendar.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaScheduleRepository extends JpaRepository<Schedule, Long>, ScheduleRepository {


}
