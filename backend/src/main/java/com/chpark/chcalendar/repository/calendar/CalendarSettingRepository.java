package com.chpark.chcalendar.repository.calendar;

import com.chpark.chcalendar.entity.calendar.CalendarSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarSettingRepository extends JpaRepository<CalendarSettingEntity, Long> {

    Optional<CalendarSettingEntity> findByUserIdAndCalendarId(Long userId, Long calendarId);

    void deleteByUserIdAndCalendarId(Long userId, Long calendarId);

    List<CalendarSettingEntity> findByUserId(Long userId);
}
