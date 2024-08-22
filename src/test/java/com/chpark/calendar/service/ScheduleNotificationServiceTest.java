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

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    public ScheduleNotificationDto.Response createNotification() {

        //given
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setTitle("hello world");
        scheduleDto.setDescription("just test");
        scheduleDto.setStartAt(LocalDateTime.now());
        scheduleDto.setEndAt(LocalDateTime.now().plusDays(3));

        ScheduleEntity scheduleEntity = new ScheduleEntity(scheduleDto);
        scheduleEntity = scheduleRepository.save(scheduleEntity);

        ScheduleNotificationDto requestNotification = new ScheduleNotificationDto(LocalDateTime.now());

        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(scheduleEntity.getId())) {
            //when
            ScheduleNotificationDto.Response createResponse = notificationService.create(scheduleEntity.getId(), requestNotification);

            //then
            assertNotNull(createResponse, "Notification has not been created.");
            log.info("Created notification: {}", createResponse);

            return createResponse;
        }

        throw new IllegalArgumentException("id that does not exist.");
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
        ScheduleNotificationDto.Response createdNotification = createNotification();

        if(createdNotification != null) {
            //when
            Optional<ScheduleNotificationDto.Response> response = notificationService.findById(createdNotification.getId());

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
        ScheduleNotificationDto.Response response = createNotification();
        List<ScheduleNotificationDto.Response> findResponse = new ArrayList<>();

        //when
        findResponse = notificationService.findByScheduleId(response.getScheduleId());

        //then
        Assert.notEmpty(findResponse, "Not found notifications");
    }

    @Test
    @Transactional
    void update() {
        //given
        ScheduleNotificationDto.Response updateResponse = createNotification();

        //when
        ScheduleNotificationDto updateRequest = new ScheduleNotificationDto(LocalDateTime.now().plusDays(30));
        updateResponse = notificationService.update(updateResponse.getId(), updateRequest);

        //then
        Assert.isTrue(updateResponse != null,"Not Updated ScheduleNotification");
        log.info("Updated notification: {}", updateResponse);
    }

    @Test
    @Transactional
    void deleteById() {
        //given
        ScheduleNotificationDto.Response deleteResponse = createNotification();

        //when
        notificationService.deleteById(deleteResponse.getId());

        //then
        Optional<ScheduleNotificationDto.Response> response = notificationService.findById(deleteResponse.getId());
        Assert.isTrue(response.isEmpty(), "Not Removed Notification");
        log.info("Removed ScheduleNotification");
    }

    @Test
    @Transactional
    void deleteNotifications() {
        //given
        ScheduleNotificationDto.Response deleteResponse = createNotification();

        //when
        notificationService.deleteByScheduleId(deleteResponse.getScheduleId());

        //then
        List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(deleteResponse.getScheduleId());
        Assert.isTrue(response.isEmpty(), "Not Removed Notification");
        log.info("Removed ScheduleNotifications");
    }

    @Test
    @Transactional
    void cascadeRemoveTest() {
        //given
        ScheduleNotificationDto.Response deleteResponse = createNotification();

        //when
        scheduleService.deleteById(deleteResponse.getScheduleId());

        //then
        List<ScheduleNotificationDto.Response> response = notificationService.findByScheduleId(deleteResponse.getScheduleId());
        Optional<ScheduleDto> scheduleDto = scheduleService.findById(deleteResponse.getScheduleId());

        Assert.isTrue(response.isEmpty(), "Not Removed Notification");
        Assert.isTrue(scheduleDto.isEmpty(), "Not Removed Notification");
        log.info("Removed Schedule and ScheduleNotifications");
    }

    @Test
    @Transactional
    void existsTest() {
        //given
        ScheduleNotificationDto.Response response = createNotification();

        //then
        Assert.isTrue(notificationService.existsById(response.getId()), "Not Found Notification");
        Assert.isTrue(notificationService.existsByScheduleId(response.getScheduleId()), "Not Found Schedule");
    }

}