package com.chpark.chcalendar.service;

import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.service.schedule.ScheduleNotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Slf4j
class ScheduleNotificationServiceTest {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleNotificationService notificationService;

    private ScheduleEntity schedule;
    private List<ScheduleNotificationDto> notificationDtoList;

    @BeforeEach
    @Transactional
    void setup() {
        schedule = new ScheduleEntity();
        schedule.setId(1);
        schedule.setTitle("Test Schedule");
        schedule.setUserId(Long.MAX_VALUE);
        schedule.setStartAt(LocalDateTime.now());
        schedule.setEndAt(LocalDateTime.now().plusDays(1));
        schedule.setRepeatId(null);
        schedule.setCalendarId(Long.MAX_VALUE);

        schedule = scheduleRepository.save(schedule);

        ScheduleNotificationDto notificationDto = new ScheduleNotificationDto();
        notificationDto.setNotificationAt(LocalDateTime.now().plusHours(1));

        notificationDtoList = new ArrayList<>();
        notificationDtoList.add(notificationDto);
        notificationDto.setNotificationAt(LocalDateTime.now().plusHours(1));

    }

    @Test
    @Transactional
    void create() {
        notificationService.create(schedule.getId(), notificationDtoList);

        List<ScheduleNotificationDto> notifications = notificationService.findByScheduleId(schedule.getId());

        assertThat(notifications).isNotEmpty();
    }

    @Test
    @Transactional
    void create_notificationsNull() {
        notificationService.create(Long.MAX_VALUE, null);

        List<ScheduleNotificationDto> notifications = notificationService.findByScheduleId(schedule.getId());

        assertThat(notifications).isEmpty();
    }

    @Test
    @Transactional
    void create_notificationsIsEmpty() {
        notificationService.create(Long.MAX_VALUE, new ArrayList<>());

        List<ScheduleNotificationDto> notifications = notificationService.findByScheduleId(schedule.getId());

        assertThat(notifications).isEmpty();
    }

    @Test
    @Transactional
    void create_EntityNotFoundException() {
        List<ScheduleNotificationDto> notifications = notificationService.findByScheduleId(schedule.getId());

        //when & then
        assertThatThrownBy(() -> notificationService.create(Long.MAX_VALUE, notificationDtoList))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Schedule not found with id: " + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    void update() {
        //given
        notificationService.create(schedule.getId(), notificationDtoList);

        ScheduleNotificationDto notificationDto = new ScheduleNotificationDto();
        notificationDto.setNotificationAt(LocalDateTime.now().plusHours(2));

        notificationDtoList.add(notificationDto);

        //소지한 개수, 추가할 개수에 대한 update 테스트
        notificationService.update(schedule.getId(), notificationDtoList);
        List<ScheduleNotificationDto> notifications = notificationService.findByScheduleId(schedule.getId());
        assertThat(notifications).hasSize(2);

        //소지한 개수 > 추가 개수일 때의 알림 삭제 테스트
        notificationDtoList = new ArrayList<>();
        notificationService.update(schedule.getId(), notificationDtoList);
        notifications = notificationService.findByScheduleId(schedule.getId());
        assertThat(notifications).isEmpty();


    }

}