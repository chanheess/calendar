package com.chpark.calendar.repository.schedule;

import com.chpark.calendar.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Integer> {

    List<ScheduleEntity> findByTitleContaining(String title);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.startAt <= :end AND s.endAt >= :start")
    List<ScheduleEntity> findSchedules(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.repeatId = :repeatId AND s.startAt > :startAt")
    List<ScheduleEntity> findFutureRepeatSchedules(@Param("repeatId") Integer repeatId, @Param("startAt") LocalDateTime startAt);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.repeatId = :repeatId AND s.startAt >= :startAt")
    List<ScheduleEntity> findCurrentAndFutureRepeatSchedules(@Param("repeatId") Integer repeatId, @Param("startAt") LocalDateTime startAt);

    @Query("SELECT COUNT(s) = 1 FROM ScheduleEntity s WHERE s.repeatId = :repeatId")
    boolean isLastRemainingRepeatSchedule(@Param("repeatId") Integer repeatId);

    @Query("SELECT s.repeatId FROM ScheduleEntity s WHERE s.id = :id")
    Optional<Integer> findRepeatIdById(@Param("id") int id);

}
