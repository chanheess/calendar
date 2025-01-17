package com.chpark.chcalendar.repository;

import com.chpark.chcalendar.entity.CalendarInfoEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarInfoRepository extends JpaRepository<CalendarInfoEntity, Long> {
    @Query("SELECT COUNT(s) FROM CalendarInfoEntity s WHERE s.adminId = :userId")
    Integer countAdminGroups(@Param("userId")Long userId);

    List<CalendarInfoEntity> findByTitleAndCategory(String title, CalendarCategory category);
    List<CalendarInfoEntity> findByAdminIdAndCategory(Long adminId, CalendarCategory category);

    @Query("SELECT c.id FROM CalendarInfoEntity c WHERE c.adminId = :adminId AND c.category = :category")
    List<Long> findIdByAdminIdAndCategory(@Param("adminId") Long adminId, @Param("category") CalendarCategory category);

    Optional<CalendarInfoEntity> findByIdAndCategory(Long id, CalendarCategory category);
    Optional<CalendarInfoEntity> findByIdAndAdminId(Long id, Long adminId);
}
