package com.chpark.chcalendar.repository.calendar;

import com.chpark.chcalendar.entity.calendar.CalendarMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarMemberRepository extends JpaRepository<CalendarMemberEntity, Long> {

    @Query("SELECT cm.calendar.id FROM CalendarMemberEntity cm WHERE cm.user.id = :userId")
    List<Long> findCalendarIdByUserId(@Param("userId") Long userId);

    @Query("SELECT cm.user.id FROM CalendarMemberEntity cm WHERE cm.calendar.id = :calendarId")
    List<Long> findUserIdByCalendarId(@Param("calendarId") Long calendarId);

    Optional<CalendarMemberEntity> findByUserIdAndCalendarId(Long userId, Long calendarId);

    List<CalendarMemberEntity> findByCalendarId(Long calendarId);

    long countByCalendarId(long calendarId);

    @Query("SELECT c FROM CalendarMemberEntity c " +
            "WHERE c.calendar.id = :calendarId AND c.user.id <> :excludeUserId " +
            "ORDER BY c.role ASC")
    List<CalendarMemberEntity> findByNextOwner(@Param("calendarId") Long calendarId, @Param("excludeUserId") Long excludeUserId);
}
