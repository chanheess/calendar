package com.chpark.calendar.repository.schedule;

import com.chpark.calendar.entity.schedule.ScheduleNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleNotificationRepository extends JpaRepository<ScheduleNotificationEntity, Long> {

    List<ScheduleNotificationEntity> findByScheduleId(long id);

    void deleteByScheduleId(long scheduleId);

    boolean existsByScheduleId(long scheduleId);

}
