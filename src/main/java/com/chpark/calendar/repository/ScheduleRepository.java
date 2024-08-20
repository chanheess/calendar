package com.chpark.calendar.repository;

import com.chpark.calendar.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Integer> {

    List<ScheduleEntity> findByTitleContaining(String title);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.startAt <= :end AND s.endAt >= :start")
    List<ScheduleEntity> findSchedules(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.repeatId = :repeatId AND s.startAt > :startAt")
    List<ScheduleEntity> findFutureRepeatSchedules(@Param("repeatId") Integer repeatId, @Param("startAt") LocalDateTime startAt);

    @Query("SELECT COUNT(s) = 0 FROM ScheduleEntity s WHERE s.repeatId = :repeatId AND s.startAt < :startAt")
    boolean existsByPreviousRepeatedSchedule(@Param("repeatId") Integer repeatId, @Param("startAt") LocalDateTime startAt);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.repeatId = :repeatId")
    List<ScheduleEntity> findSchedulesByRepeatId(@Param("repeatId") Integer repeatId);

}
