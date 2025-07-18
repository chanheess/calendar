package com.chpark.chcalendar.repository.calendar;

import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {
    @Query("SELECT COUNT(s) FROM CalendarEntity s WHERE s.userId = :userId")
    Integer countAdminGroups(@Param("userId")Long userId);

    List<CalendarEntity> findByUserIdAndCategory(Long userId, CalendarCategory category);

    @Query("SELECT c.id FROM CalendarEntity c WHERE c.userId = :userId AND c.category = :category")
    List<Long> findIdByUserIdAndCategory(@Param("userId") Long userId, @Param("category") CalendarCategory category);

    Optional<CalendarEntity> findByIdAndCategory(Long id, CalendarCategory category);
    Optional<CalendarEntity> findByIdAndUserId(Long id, Long userId);

    List<CalendarEntity> findByUserId(Long userId);
}
