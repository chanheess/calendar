package com.chpark.calendar.repository.schedule;

import com.chpark.calendar.entity.ScheduleNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleNotificationRepository extends JpaRepository<ScheduleNotificationEntity, Integer> {

    List<ScheduleNotificationEntity> findByScheduleId(int id);

    void deleteByScheduleId(int scheduleId);

    boolean existsByScheduleId(int scheduleId);

}
