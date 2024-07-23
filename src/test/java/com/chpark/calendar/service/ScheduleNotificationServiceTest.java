package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.dto.ScheduleNotificationDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.repository.ScheduleNotificationRepository;
import com.chpark.calendar.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Optional<ScheduleNotificationDto.Response> createNotification() {

        //given
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setTitle("hello world");
        scheduleDto.setDescription("just test");
        scheduleDto.setStartAt(LocalDateTime.now());
        scheduleDto.setEndAt(LocalDateTime.now().plusDays(3));

        ScheduleEntity scheduleEntity = new ScheduleEntity(scheduleDto);
        scheduleEntity = scheduleRepository.save(scheduleEntity);

        ScheduleNotificationDto.Request request = new ScheduleNotificationDto.Request(LocalDateTime.now());
        Optional<ScheduleNotificationDto.Response> createResponse = Optional.empty();

        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(scheduleEntity.getId())) {
            //when
            createResponse = notificationService.create(scheduleEntity.getId(), request);

            //then
            Assert.isTrue(createResponse.isPresent(), "Notification has not been created.");
            log.info("Created notification: {}", createResponse);
        } else {
            throw new IllegalArgumentException("id that does not exist.");
        }

        return createResponse;
    }

    @Test
    @Transactional
    void create() {
        createNotification();
    }

    @Test
    @Transactional
    void findById() {
        //given
        Optional<ScheduleNotificationDto.Response> response = createNotification();

        if(response.isPresent()) {
            //when
            response = notificationService.findById(response.get().getId());

            //then
            Assert.isTrue(response.isPresent(), "Not found notifications");
            log.info("Found notification: {}", response);
        } else {
            throw new IllegalArgumentException("Notification has not been created.");
        }
    }

    @Test
    @Transactional
    void findByScheduleId() {
        //given
        Optional<ScheduleNotificationDto.Response> response = createNotification();
        List<ScheduleNotificationDto.Response> findResponse = new ArrayList<>();

        if(response.isPresent()) {
            //when
            findResponse = notificationService.findByScheduleId(response.get().getScheduleId());

            //then
            Assert.notEmpty(findResponse, "Not found notifications");
            findResponse.forEach(schedule -> log.info("Found Notifications: {}", schedule));
        } else {
            throw new IllegalArgumentException("Notification has not been created.");
        }
    }

    @Test
    @Transactional
    void update() {
        //given
        Optional<ScheduleNotificationDto.Response> updateResponse = createNotification();

        if(updateResponse.isPresent()) {
            //when
            ScheduleNotificationDto.Request updateRequest = new ScheduleNotificationDto.Request(LocalDateTime.now().plusDays(30));
            updateResponse = notificationService.update(updateResponse.get().getId(), updateRequest);

            //then
            Assert.isTrue(updateResponse.isPresent(),"Not Updated ScheduleNotification");
            log.info("Updated notification: {}", updateResponse);
        } else {
            throw new IllegalArgumentException("Notification has not been created.");
        }
    }

    @Test
    @Transactional
    void deleteById() {
        //given
        Optional<ScheduleNotificationDto.Response> deleteResponse = createNotification();

        if(deleteResponse.isPresent()) {
            //when
            notificationService.deleteById(deleteResponse.get().getId());

            //then
            Optional<ScheduleNotificationDto.Response> response = notificationService.findById(deleteResponse.get().getId());
            Assert.isTrue(response.isEmpty(), "Not Removed Notification");
            log.info("Removed ScheduleNotification");
        } else {
            throw new IllegalArgumentException("Notification has not been created.");
        }
    }

    @Test
    @Transactional
    void deleteNotifications() {
        //given
        Optional<ScheduleNotificationDto.Response> deleteResponse = createNotification();

        if(deleteResponse.isPresent()) {
            //when
            notificationService.deleteByScheduleId(deleteResponse.get().getScheduleId());

            //then
            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(deleteResponse.get().getScheduleId());
            Assert.isTrue(response.isEmpty(), "Not Removed Notification");
            log.info("Removed ScheduleNotifications");
        } else {
            throw new IllegalArgumentException("Notification has not been created.");
        }
    }

    @Test
    @Transactional
    void cascadeRemoveTest() {
        //given
        Optional<ScheduleNotificationDto.Response> deleteResponse = createNotification();

        //일정을 삭제해서 연결되어 있는 알림들이 자동으로 삭제
        if(deleteResponse.isPresent()) {
            //when
            scheduleRepository.deleteById(deleteResponse.get().getScheduleId());

            //then
            List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(deleteResponse.get().getScheduleId());
            Optional<ScheduleDto> scheduleDto = scheduleService.findById(deleteResponse.get().getScheduleId());

            Assert.isTrue(response.isEmpty(), "Not Removed Notification");
            Assert.isTrue(scheduleDto.isEmpty(), "Not Removed Notification");
            log.info("Removed Schedule and ScheduleNotifications");
        } else {
            throw new IllegalArgumentException("Notification has not been created.");
        }

    }

    @Test
    @Transactional
    void existsTest() {
        //given
        Optional<ScheduleNotificationDto.Response> response = createNotification();

        //then
        if(response.isPresent()) {
            Assert.isTrue(notificationService.existsById(response.get().getId()), "Not Found Notification");
            Assert.isTrue(notificationService.existsByScheduleId(response.get().getScheduleId()), "Not Found Schedule");
        } else {
            throw new IllegalArgumentException("Notification has not been created.");
        }
    }

}