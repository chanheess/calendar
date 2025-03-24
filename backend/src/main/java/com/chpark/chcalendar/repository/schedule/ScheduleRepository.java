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

    @Query("""
        SELECT DISTINCT s
          FROM ScheduleEntity s
          LEFT JOIN ScheduleGroupEntity sg
                 ON s.id = sg.scheduleId
         WHERE (sg IS NULL OR s.userId = :userId OR sg.userId = :userId)
           AND s.calendarId IN :calendarIds
           AND s.startAt <= :end
           AND s.endAt >= :start
           AND (s.startAt > :cursorStartAt OR (s.startAt = :cursorStartAt AND s.id > :cursorId))
      ORDER BY s.startAt ASC, s.id ASC
    """)
    List<ScheduleEntity> findSchedulesByCalendarIdAndUser(
            @Param("userId") Long userId,
            @Param("calendarIds") List<Long> calendarIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cursorStartAt") LocalDateTime cursorStartAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("SELECT s FROM ScheduleEntity s WHERE s.repeatId = :repeatId AND s.startAt > :startAt AND s.userId = :userId")
    List<ScheduleEntity> findFutureRepeatSchedules(@Param("repeatId") Long repeatId, @Param("startAt") LocalDateTime startAt, @Param("userId") Long userId);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.repeatId = :repeatId AND s.startAt >= :startAt AND s.userId = :userId")
    List<ScheduleEntity> findCurrentAndFutureRepeatSchedules(@Param("repeatId") Long repeatId, @Param("startAt") LocalDateTime startAt, @Param("userId") Long userId);

    @Query("SELECT COUNT(s) = 1 FROM ScheduleEntity s WHERE s.repeatId = :repeatId")
    boolean isLastRemainingRepeatSchedule(@Param("repeatId") Long repeatId);

    @Query("SELECT s.repeatId FROM ScheduleEntity s WHERE s.id = :scheduleId AND s.userId = :userId")
    Optional<Long> getRepeatId(@Param("scheduleId") Long scheduleId, @Param("userId") Long userId);

    List<ScheduleEntity> findByUserId(@Param("userId") Long userId);

    Optional<ScheduleEntity> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    void deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);



}
