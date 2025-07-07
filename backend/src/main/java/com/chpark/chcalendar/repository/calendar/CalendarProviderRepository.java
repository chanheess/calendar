package com.chpark.chcalendar.repository.calendar;

import com.chpark.chcalendar.entity.calendar.CalendarProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalendarProviderRepository extends JpaRepository<CalendarProviderEntity, Long> {

    Optional<CalendarProviderEntity> findByCalendarId(Long calendarId);
}
