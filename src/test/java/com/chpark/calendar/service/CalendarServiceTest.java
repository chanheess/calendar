package com.chpark.calendar.service;

import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.repository.CalendarRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@EnableTransactionManagement
public class CalendarServiceTest {

    @Autowired CalendarService calendarService;
    @Autowired CalendarRepository calendarRepository;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(CalendarServiceTest.class);

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

    @Test
    @Transactional
    void testFindSchedulesByTitle() {
        ScheduleEntity scheduleEntity1 = new ScheduleEntity();
        scheduleEntity1.setTitle("나 찾아봐라~");
        scheduleEntity1.setDescription("이게 머고?");
        scheduleEntity1.setStartTime(LocalDateTime.now());
        scheduleEntity1.setEndTime(LocalDateTime.now().plusDays(3));
        calendarRepository.save(scheduleEntity1);

        ScheduleEntity scheduleEntity2 = new ScheduleEntity();
        scheduleEntity2.setTitle("나는 못  찾겠지ㅋㅋ");
        scheduleEntity2.setDescription("이건 맞지^^");
        scheduleEntity2.setStartTime(LocalDateTime.now());
        scheduleEntity2.setEndTime(LocalDateTime.now().plusDays(4));
        calendarRepository.save(scheduleEntity2);

        List<ScheduleEntity> result = calendarService.findSchedulesByTitle("나");
        assertFalse(result.isEmpty());

        result.forEach(schedule -> logger.info("Found Schedule: {}", schedule));

        result = calendarService.findSchedulesByTitle("나는");
        assertFalse(result.isEmpty());

        result.forEach(schedule -> logger.info("Found Schedule: {}", schedule));
    }
}
