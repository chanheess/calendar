package com.chpark.chcalendar.repository.calendar;

import com.chpark.chcalendar.entity.calendar.CalendarExternalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalendarExternalRepository extends JpaRepository<CalendarExternalEntity, Long> {

    Optional<CalendarExternalEntity> findByCalendarId(Long calendarId);
}
