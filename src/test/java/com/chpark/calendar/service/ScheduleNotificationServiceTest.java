package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.dto.ScheduleNotificationDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.entity.ScheduleNotificationEntity;
import com.chpark.calendar.repository.ScheduleNotificationRepository;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableTransactionManagement
@Slf4j
class ScheduleNotificationServiceTest {

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    ScheduleNotificationService notificationService;

    @Autowired
    ScheduleNotificationRepository notificationRepository;

    //일정 생성 메서드
    public ScheduleNotificationEntity createSchedule() {
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setTitle("hello world");
        scheduleDto.setDescription("just test");
        scheduleDto.setStartAt(LocalDateTime.now());
        scheduleDto.setEndAt(LocalDateTime.now().plusDays(3));

        ScheduleEntity scheduleEntity = new ScheduleEntity(scheduleDto);
        scheduleRepository.save(scheduleEntity);

        ScheduleNotificationEntity scheduleNotificationEntity = new ScheduleNotificationEntity();
        scheduleNotificationEntity.setNotificationAt(LocalDateTime.now().plusDays(3));
        scheduleNotificationEntity.setScheduleId(scheduleEntity.getId());

        return scheduleNotificationEntity;
    }

    @Test
    @Transactional
    void createAndFindByScheduleId() {
        ScheduleNotificationEntity scheduleEntity = createSchedule();
        ScheduleNotificationDto.Request request = new ScheduleNotificationDto.Request(scheduleEntity);

        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(scheduleEntity.getScheduleId())) {
            notificationService.create(scheduleEntity.getScheduleId(), request);

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(scheduleEntity.getScheduleId());
            response.forEach(schedule -> log.info("Found Schedule: {}", schedule));
        }
        else {
            log.info("Not Found ScheduleNotification");
        }
    }

    @Test
    @Transactional
    void update() {
        ScheduleNotificationEntity createEntity = createSchedule();
        ScheduleNotificationDto.Request request = new ScheduleNotificationDto.Request(createEntity);
        Optional<ScheduleNotificationDto.Response> resultResponse = Optional.empty();
        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(createEntity.getScheduleId())) {
            resultResponse = notificationService.create(createEntity.getScheduleId(), request);

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(createEntity.getScheduleId());
            response.forEach(schedule -> log.info("Found ScheduleNotification: {}", schedule));
        }
        else {
            log.info("Not Found ScheduleNotification");
        }

        createEntity.setNotificationAt(LocalDateTime.now().plusDays(30));
        ScheduleNotificationDto.Request updateRequest = new ScheduleNotificationDto.Request(createEntity);

        //생성된 값이 있다면 해당 값 사용
        if(resultResponse.isPresent()) {
            Optional<ScheduleNotificationDto.Response> response = notificationService.update(resultResponse.get().getId(), updateRequest);

            if(response.isPresent()) {
                log.info("Updated ScheduleNotification: {}", response.get());
            } else {
                log.info("Not Updated ScheduleNotification");
            }
        } else {
            log.info("Not Found ScheduleNotification");
        }
    }

    @Test
    @Transactional
    void deleteById() {
        ScheduleNotificationEntity createEntity = createSchedule();
        ScheduleNotificationDto.Request request = new ScheduleNotificationDto.Request(createEntity);
        Optional<ScheduleNotificationDto.Response> resultResponse = Optional.empty();

        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(createEntity.getScheduleId())) {
            resultResponse = notificationService.create(createEntity.getScheduleId(), request);

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(createEntity.getScheduleId());
            response.forEach(schedule -> log.info("Found ScheduleNotification: {}", schedule));
        }
        else {
            log.info("Not Found ScheduleNotification");
        }

        //생성된 값이 있다면 해당 값 사용
        if(resultResponse.isPresent()) {
            notificationService.deleteById(resultResponse.get().getId());

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(createEntity.getScheduleId());
            if(response.isEmpty()) {
                log.info("Removed ScheduleNotification");
            } else {
                response.forEach(schedule -> log.info("Not Removed, ScheduleNotification: {}", schedule));
            }
        } else {
            log.info("Not Found ScheduleNotification");
        }
    }

    @Test
    @Transactional
    void deleteNotifications() {
        ScheduleNotificationEntity createEntity = createSchedule();
        ScheduleNotificationDto.Request request = new ScheduleNotificationDto.Request(createEntity);
        Optional<ScheduleNotificationDto.Response> resultResponse = Optional.empty();

        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(createEntity.getScheduleId())) {
            //두 번생성
            notificationService.create(createEntity.getScheduleId(), request);
            resultResponse = notificationService.create(createEntity.getScheduleId(), request);

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(createEntity.getScheduleId());
            response.forEach(schedule -> log.info("Found ScheduleNotification: {}", schedule));
        }
        else {
            log.info("Not Found ScheduleNotification");
        }

        //생성된 값이 있다면 해당 값 사용
        if(resultResponse.isPresent()) {
            notificationService.deleteNotifications(resultResponse.get().getScheduleId());

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(createEntity.getScheduleId());
            if(response.isEmpty()) {
                log.info("All Removed ScheduleNotification");
            } else {
                response.forEach(schedule -> log.info("Not Removed, ScheduleNotification: {}", schedule));
            }
        } else {
            log.info("Not Found ScheduleNotification");
        }
    }
}