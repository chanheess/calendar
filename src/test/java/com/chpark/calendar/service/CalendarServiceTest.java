package com.chpark.calendar.service;

import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.repository.CalendarRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;

@SpringBootTest
@EnableTransactionManagement
public class CalendarServiceTest {

    @Autowired CalendarService calendarService;
    @Autowired CalendarRepository calendarRepository;

    @Test
    @Transactional
    void join() {
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setTitle("hello world");
        scheduleEntity.setDescription("just test");
        scheduleEntity.setStartTime(LocalDateTime.now());
        scheduleEntity.setEndTime(LocalDateTime.now().plusDays(3));

        calendarService.create(scheduleEntity);
    }
}
