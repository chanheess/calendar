package com.chpark.calendar.repository;

import com.chpark.calendar.entity.CalendarInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarInfoRepository extends JpaRepository<CalendarInfoEntity, Long> {

    List<CalendarInfoEntity> findByUserId(long userId);
}
