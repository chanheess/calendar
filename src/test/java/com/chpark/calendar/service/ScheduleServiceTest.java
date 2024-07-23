package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@EnableTransactionManagement
@Slf4j
public class ScheduleServiceTest {

    @Autowired
    ScheduleService scheduleService;
    @Autowired
    ScheduleRepository scheduleRepository;

    @Test
    @Transactional
    void join() {
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setTitle("hello world");
        scheduleDto.setDescription("just test");
        scheduleDto.setStartAt(LocalDateTime.now());
        scheduleDto.setEndAt(LocalDateTime.now().plusDays(3));

        Optional<ScheduleDto> createDto = scheduleService.create(scheduleDto);
        assertFalse(createDto.isEmpty(), "Not created");

        log.info("Created schedule info: {}", createDto);
    }

    @Test
    @Transactional
    void testFindSchedulesByTitle() {
        ScheduleEntity scheduleEntity1 = new ScheduleEntity();
        scheduleEntity1.setTitle("나 찾아봐라~");
        scheduleEntity1.setDescription("이게 머고?");
        scheduleEntity1.setStartAt(LocalDateTime.now());
        scheduleEntity1.setEndAt(LocalDateTime.now().plusDays(3));
        scheduleRepository.save(scheduleEntity1);

        ScheduleEntity scheduleEntity2 = new ScheduleEntity();
        scheduleEntity2.setTitle("나는 못  찾겠지ㅋㅋ");
        scheduleEntity2.setDescription("이건 맞지^^");
        scheduleEntity2.setStartAt(LocalDateTime.now());
        scheduleEntity2.setEndAt(LocalDateTime.now().plusDays(4));
        scheduleRepository.save(scheduleEntity2);

        List<ScheduleDto> result = scheduleService.findSchedulesByTitle("나");
        assertFalse(result.isEmpty());

        result.forEach(schedule -> log.info("Found Schedule: {}", schedule));
    }

    @Test
    @Transactional
    void updateTest() {
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setTitle("hello world");
        scheduleEntity.setDescription("just test");
        scheduleEntity.setStartAt(LocalDateTime.now());
        scheduleEntity.setEndAt(LocalDateTime.now().plusDays(3));
        scheduleRepository.save(scheduleEntity);

        scheduleEntity.setTitle("hi cloud");
        scheduleEntity.setDescription("update?");
        scheduleEntity.setStartAt(LocalDateTime.now());
        scheduleEntity.setEndAt(LocalDateTime.now().plusDays(8));

        Optional<ScheduleDto> updateDto = scheduleService.update(scheduleEntity.getId(), new ScheduleDto(scheduleEntity));
        assertFalse(updateDto.isEmpty());
        log.info("Updated Schedule: {}", updateDto);
    }

    @Test
    @Transactional
    void deleteTest() {
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setTitle("delete test");
        scheduleEntity.setStartAt(LocalDateTime.now());
        scheduleEntity.setEndAt(LocalDateTime.now().plusDays(8));

        scheduleRepository.save(scheduleEntity);

        int deletedId = scheduleEntity.getId();
        scheduleService.deleteById(scheduleEntity.getId());

        Optional<ScheduleEntity> result = scheduleRepository.findById(deletedId);
        assertFalse(result.isPresent(), "Not deleted.");

        log.info("Schedule with ID {} was successfully deleted.", deletedId);
    }

    @Test
    @Transactional
    void scheduleSearching() {
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setTitle("schedule searching test");
        scheduleEntity.setStartAt(LocalDateTime.now());
        scheduleEntity.setEndAt(LocalDateTime.now().plusDays(8));

        scheduleRepository.save(scheduleEntity);

        List<ScheduleDto> dateList = scheduleService.getSchedulesForDate(LocalDateTime.now().getYear(), LocalDateTime.now().getMonthValue(), LocalDateTime.now().getDayOfMonth());
        assertFalse(dateList.isEmpty());
        dateList.forEach(schedule -> log.info("Found Date Schedule: {}", schedule));

        List<ScheduleDto> monthList = scheduleService.getSchedulesForMonth(LocalDateTime.now().getYear(), LocalDateTime.now().getMonthValue());
        assertFalse(monthList.isEmpty());
        monthList.forEach(schedule -> log.info("Found Month Schedule: {}", schedule));

        List<ScheduleDto> yearList = scheduleService.getSchedulesForYear(LocalDateTime.now().getYear());
        assertFalse(yearList.isEmpty());
        yearList.forEach(schedule -> log.info("Found Year Schedule: {}", schedule));
    }

    @Test
    @Transactional
    void scheduleExistsById() {
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setTitle("schedule exists test");
        scheduleEntity.setStartAt(LocalDateTime.now());
        scheduleEntity.setEndAt(LocalDateTime.now().plusDays(8));

        scheduleRepository.save(scheduleEntity);

        assertFalse(!scheduleService.existsById(scheduleEntity.getId()), "Not Found");
        log.info("Found Schedule: {}", scheduleEntity);
    }

}
