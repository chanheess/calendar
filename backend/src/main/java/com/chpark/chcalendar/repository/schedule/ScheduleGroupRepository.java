package com.chpark.chcalendar.repository.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleGroupRepository extends JpaRepository<ScheduleGroupEntity, Long> {


    Optional<ScheduleGroupEntity> findByScheduleIdAndUserId(long scheduleId, long userId);

    List<ScheduleGroupEntity> findByScheduleId(long scheduleId);

    void deleteByScheduleId(long scheduleId);

}
