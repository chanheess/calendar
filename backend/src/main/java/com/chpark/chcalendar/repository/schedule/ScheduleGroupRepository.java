package com.chpark.chcalendar.repository.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScheduleGroupRepository extends JpaRepository<ScheduleGroupEntity, Long> {


    Optional<ScheduleGroupEntity> findByScheduleIdAndUserId(long scheduleId, long userId);

    List<ScheduleGroupEntity> findByScheduleId(long scheduleId);

    void deleteByScheduleId(long scheduleId);

    long countByScheduleId(long scheduleId);

    @Query("SELECT s FROM ScheduleGroupEntity s " +
            "WHERE s.scheduleId = :scheduleId AND s.userId <> :excludeUserId " +
            "ORDER BY s.authority ASC") // ASC: ordinal이 낮을수록 높은 권한
    List<ScheduleGroupEntity> findByScheduleIdAndUserIdNotOrderByAuthorityAsc(long scheduleId, long excludeUserId);
}
