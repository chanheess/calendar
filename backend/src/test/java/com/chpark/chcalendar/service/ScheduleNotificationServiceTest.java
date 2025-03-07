package com.chpark.chcalendar.service;

import com.chpark.chcalendar.DotenvInitializer;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Slf4j
@ContextConfiguration(initializers = DotenvInitializer.class)
class ScheduleNotificationServiceTest {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @SpyBean
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
        doNothing().when(notificationService).createNotificationScheduler(anyLong(), any(ScheduleEntity.class), any());

        notificationService.create(schedule.getUserId(), schedule.getId(), notificationDtoList);
        List<ScheduleNotificationDto> notifications = notificationService.findByScheduleId(schedule.getId());

        assertThat(notifications).isNotEmpty();
    }

    @Test
    @Transactional
    void create_notificationsNull() {
        doNothing().when(notificationService).createNotificationScheduler(anyLong(), any(ScheduleEntity.class), any());

        notificationService.create(schedule.getUserId(), Long.MAX_VALUE, null);
        List<ScheduleNotificationDto> notifications = notificationService.findByScheduleId(schedule.getId());

        assertThat(notifications).isEmpty();
    }

    @Test
    @Transactional
    void create_notificationsIsEmpty() {
        doNothing().when(notificationService).createNotificationScheduler(anyLong(), any(ScheduleEntity.class), any());

        notificationService.create(schedule.getUserId(), Long.MAX_VALUE, new ArrayList<>());
        List<ScheduleNotificationDto> notifications = notificationService.findByScheduleId(schedule.getId());

        assertThat(notifications).isEmpty();
    }

    @Test
    @Transactional
    void create_EntityNotFoundException() {
        doNothing().when(notificationService).createNotificationScheduler(anyLong(), any(ScheduleEntity.class), any());
        List<ScheduleNotificationDto> notifications = notificationService.findByScheduleId(schedule.getId());

        //when & then
        assertThatThrownBy(() -> notificationService.create(schedule.getUserId(), Long.MAX_VALUE, notificationDtoList))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Schedule not found with id: " + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    void update() {
        //given
        doNothing().when(notificationService).createNotificationScheduler(anyLong(), any(ScheduleEntity.class), any());
        doNothing().when(notificationService).updateNotificationScheduler(anyLong(), any(ScheduleEntity.class), any());
        doNothing().when(notificationService).deleteNotificationScheduler(anyLong(), any(ScheduleEntity.class), any());

        notificationService.create(schedule.getUserId(), schedule.getId(), notificationDtoList);

        ScheduleNotificationDto notificationDto = new ScheduleNotificationDto();
        notificationDto.setNotificationAt(LocalDateTime.now().plusHours(2));

        notificationDtoList.add(notificationDto);

        //소지한 개수, 추가할 개수에 대한 update 테스트
        notificationService.update(schedule.getUserId(), schedule.getId(), notificationDtoList);
        List<ScheduleNotificationDto> notifications = notificationService.findByScheduleId(schedule.getId());
        assertThat(notifications).hasSize(2);

        //소지한 개수 > 추가 개수일 때의 알림 삭제 테스트
        notificationDtoList = new ArrayList<>();
        notificationService.update(schedule.getUserId(), schedule.getId(), notificationDtoList);
        notifications = notificationService.findByScheduleId(schedule.getId());
        assertThat(notifications).isEmpty();


    }

}