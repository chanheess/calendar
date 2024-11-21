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

    List<ScheduleEntity> findByTitleContainingAndUserId(@Param("title")String title, @Param("userId") Integer userId);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.startAt <= :end AND s.endAt >= :start AND s.userId = :userId")
    List<ScheduleEntity> findSchedules(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("userId") Integer userId);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.repeatId = :repeatId AND s.startAt > :startAt AND s.userId = :userId")
    List<ScheduleEntity> findFutureRepeatSchedules(@Param("repeatId") Integer repeatId, @Param("startAt") LocalDateTime startAt, @Param("userId") Integer userId);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.repeatId = :repeatId AND s.startAt >= :startAt AND s.userId = :userId")
    List<ScheduleEntity> findCurrentAndFutureRepeatSchedules(@Param("repeatId") Integer repeatId, @Param("startAt") LocalDateTime startAt, @Param("userId") Integer userId);

    @Query("SELECT COUNT(s) = 1 FROM ScheduleEntity s WHERE s.repeatId = :repeatId")
    boolean isLastRemainingRepeatSchedule(@Param("repeatId") Integer repeatId);

    @Query("SELECT s.repeatId FROM ScheduleEntity s WHERE s.id = :scheduleId AND s.userId = :userId")
    Optional<Integer> getRepeatId(@Param("scheduleId") Integer scheduleId, @Param("userId") Integer userId);

    List<ScheduleEntity> findByUserId(@Param("userId") Integer userId);

    Optional<ScheduleEntity> findByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);

    void deleteByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);

}
