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
    public ScheduleEntity createSchedule() {
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setTitle("hello world");
        scheduleDto.setDescription("just test");
        scheduleDto.setStartAt(LocalDateTime.now());
        scheduleDto.setEndAt(LocalDateTime.now().plusDays(3));

        ScheduleEntity scheduleEntity = new ScheduleEntity(scheduleDto);
        scheduleRepository.save(scheduleEntity);

        return scheduleEntity;
    }

    @Test
    @Transactional
    void createAndFindByScheduleId() {
        ScheduleEntity scheduleEntity = createSchedule();
        ScheduleNotificationDto.Request request = new ScheduleNotificationDto.Request(LocalDateTime.now());

        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(scheduleEntity.getId())) {
            notificationService.create(scheduleEntity.getId(), request);

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(scheduleEntity.getId());
            response.forEach(schedule -> log.info("Found Schedule: {}", schedule));
        }
        else {
            log.info("Not Found ScheduleNotification");
        }
    }

    @Test
    @Transactional
    void update() {
        ScheduleEntity scheduleEntity = createSchedule();
        ScheduleNotificationDto.Request request = new ScheduleNotificationDto.Request(LocalDateTime.now());
        Optional<ScheduleNotificationDto.Response> resultResponse = Optional.empty();

        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(scheduleEntity.getId())) {
            resultResponse = notificationService.create(scheduleEntity.getId(), request);

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(scheduleEntity.getId());
            response.forEach(schedule -> log.info("Found ScheduleNotification: {}", schedule));
        }
        else {
            log.info("Not Found ScheduleNotification");
        }

        //생성된 값이 있다면 해당 값 사용
        if(resultResponse.isPresent()) {
            ScheduleNotificationDto.Request updateRequest = new ScheduleNotificationDto.Request(LocalDateTime.now().plusDays(30));
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
        ScheduleEntity scheduleEntity = createSchedule();
        ScheduleNotificationDto.Request request = new ScheduleNotificationDto.Request(LocalDateTime.now());
        Optional<ScheduleNotificationDto.Response> resultResponse = Optional.empty();

        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(scheduleEntity.getId())) {
            resultResponse = notificationService.create(scheduleEntity.getId(), request);

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(scheduleEntity.getId());
            response.forEach(schedule -> log.info("Found ScheduleNotification: {}", schedule));
        }
        else {
            log.info("Not Found ScheduleNotification");
        }

        //생성된 값이 있다면 해당 값 사용
        if(resultResponse.isPresent()) {
            notificationService.deleteById(resultResponse.get().getId());

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(scheduleEntity.getId());
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
        ScheduleEntity scheduleEntity = createSchedule();
        ScheduleNotificationDto.Request request = new ScheduleNotificationDto.Request(LocalDateTime.now());
        Optional<ScheduleNotificationDto.Response> resultResponse = Optional.empty();

        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(scheduleEntity.getId())) {
            //두 번생성
            notificationService.create(scheduleEntity.getId(), request);
            resultResponse = notificationService.create(scheduleEntity.getId(), request);

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(scheduleEntity.getId());
            response.forEach(schedule -> log.info("Found ScheduleNotification: {}", schedule));
        }
        else {
            log.info("Not Found ScheduleNotification");
        }

        //생성된 값이 있다면 해당 값 사용
        if(resultResponse.isPresent()) {
            notificationService.deleteNotifications(resultResponse.get().getScheduleId());

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(scheduleEntity.getId());
            if(response.isEmpty()) {
                log.info("All Removed ScheduleNotification");
            } else {
                response.forEach(schedule -> log.info("Not Removed, ScheduleNotification: {}", schedule));
            }
        } else {
            log.info("Not Found ScheduleNotification");
        }
    }

    @Test
    @Transactional
    void cascadeRemoveTest() {
        ScheduleEntity scheduleEntity = createSchedule();
        ScheduleNotificationDto.Request request = new ScheduleNotificationDto.Request(LocalDateTime.now());
        Optional<ScheduleNotificationDto.Response> resultResponse = Optional.empty();

        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(scheduleEntity.getId())) {
            //두 번생성
            notificationService.create(scheduleEntity.getId(), request);
            resultResponse = notificationService.create(scheduleEntity.getId(), request);

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(scheduleEntity.getId());
            response.forEach(schedule -> log.info("Found ScheduleNotification: {}", schedule));
        }
        else {
            log.info("Not Found ScheduleNotification");
        }

        //일정을 삭제해서 연결되어 있는 알림들이 자동으로 삭제
        if(resultResponse.isPresent()) {
            scheduleRepository.deleteById(scheduleEntity.getId());

            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(scheduleEntity.getId());
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