package com.chpark.chcalendar.repository.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {

    List<ScheduleEntity> findByTitleContainingAndUserId(@Param("title")String title, @Param("userId") Long userId);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.startAt <= :end AND s.endAt >= :start AND s.userId = :userId")
    List<ScheduleEntity> findSchedules(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("userId") Long userId);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.startAt <= :end AND s.endAt >= :start AND s.calendarId = :calendarId")
    List<ScheduleEntity> findSchedulesByCalendarId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("calendarId") Long calendarId);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.repeatId = :repeatId AND s.startAt > :startAt AND s.userId = :userId")
    List<ScheduleEntity> findFutureRepeatSchedules(@Param("repeatId") Long repeatId, @Param("startAt") LocalDateTime startAt, @Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM ScheduleEntity s WHERE s.repeatId = :repeatId")
    long countByRepeatId(@Param("repeatId") Long repeatId);

    List<ScheduleEntity> findByUserId(@Param("userId") Long userId);

    Optional<ScheduleEntity> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);




}
