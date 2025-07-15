package com.chpark.chcalendar.repository.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScheduleGroupRepository extends JpaRepository<ScheduleGroupEntity, Long> {


    Optional<ScheduleGroupEntity> findByScheduleIdAndUserId(long scheduleId, long userId);

    List<ScheduleGroupEntity> findByScheduleId(long scheduleId);

    List<ScheduleGroupEntity> findByUserId(long userId);

    void deleteByScheduleId(long scheduleId);

    long countByScheduleId(long scheduleId);

    @Query("SELECT s FROM ScheduleGroupEntity s " +
            "WHERE s.scheduleId = :scheduleId AND s.userId <> :excludeUserId " +
            "ORDER BY s.authority ASC")
    List<ScheduleGroupEntity> findByNextOwner(long scheduleId, long excludeUserId);
}
