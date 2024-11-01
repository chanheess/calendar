package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.dto.ScheduleNotificationDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.entity.ScheduleNotificationEntity;
import com.chpark.calendar.repository.schedule.ScheduleRepository;
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

    //일정 생성 메서드
    public ScheduleEntity createNotification() {
        //given
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setTitle("hello world");
        scheduleDto.setDescription("just test");
        scheduleDto.setStartAt(LocalDateTime.now());
        scheduleDto.setEndAt(LocalDateTime.now().plusDays(3));

        ScheduleEntity scheduleEntity = new ScheduleEntity(scheduleDto);
        scheduleEntity = scheduleRepository.save(scheduleEntity);

        List<ScheduleNotificationEntity> requestEntities = new ArrayList<>();
        requestEntities.add(new ScheduleNotificationEntity(scheduleEntity.getId(), LocalDateTime.now().minusHours(1)));
        requestEntities.add(new ScheduleNotificationEntity(scheduleEntity.getId(), LocalDateTime.now().minusHours(2)));
        List<ScheduleNotificationDto> requestNotifications = ScheduleNotificationDto.fromScheduleNotificationEntityList(requestEntities);

        //일정이 있을 때만 알림 생성 가능
        if(scheduleService.existsById(scheduleEntity.getId())) {
            //when
            List<ScheduleNotificationDto> createResponse = notificationService.create(scheduleEntity.getId(), requestNotifications);

            //then
            assertNotNull(createResponse, "Notification has not been created.");
            log.info("Created notification: {}", createResponse);

            return scheduleEntity;
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
    void findByScheduleId() {
        //given
        ScheduleEntity scheduleEntity = createNotification();

        List<ScheduleNotificationDto> findResponse = new ArrayList<>();

        //when
        findResponse = notificationService.findByScheduleId(scheduleEntity.getId());

        //then
        Assert.notEmpty(findResponse, "Not found notifications");
    }

    @Test
    @Transactional
    void update() {
        //given
        ScheduleEntity scheduleEntity = createNotification();

        //when
        List<ScheduleNotificationDto> updateRequest = new ArrayList<>();
        updateRequest.add(new ScheduleNotificationDto(LocalDateTime.now().plusDays(30)));
        List<ScheduleNotificationDto> updateResponse = notificationService.update(scheduleEntity.getId(), updateRequest);

        //then
        Assert.isTrue(updateResponse != null,"Not Updated ScheduleNotification");
        log.info("Updated notification: {}", updateResponse);
    }

    @Test
    @Transactional
    void deleteNotifications() {
        //given
        ScheduleEntity scheduleEntity = createNotification();

        //when
        notificationService.deleteByScheduleId(scheduleEntity.getId());

        //then
        List<ScheduleNotificationDto> response = notificationService.findByScheduleId(scheduleEntity.getId());
        Assert.isTrue(response.isEmpty(), "Not Removed Notification");
        log.info("Removed ScheduleNotifications");
    }

//    @Test
//    @Transactional
//    void cascadeRemoveTest() {
//        //given
//        ScheduleEntity scheduleEntity = createNotification();
//
//        //when
//        scheduleService.deleteById(scheduleEntity.getId());
//
//        //then
//        List<ScheduleNotificationDto> response = notificationService.findByScheduleId(scheduleEntity.getId());
//        Optional<ScheduleDto> resultScheduleDto = scheduleService.findById(scheduleEntity.getId());
//
//        Assert.isTrue(response.isEmpty(), "Not Removed Notifications");
//        Assert.isTrue(resultScheduleDto.isEmpty(), "Not Removed Schedule");
//        log.info("Removed Schedule and ScheduleNotifications");
//    }

    @Test
    @Transactional
    void existsTest() {
        //given
        ScheduleEntity scheduleEntity = createNotification();

        Assert.isTrue(notificationService.existsByScheduleId(scheduleEntity.getId()), "Not Found Notifications");
    }
}