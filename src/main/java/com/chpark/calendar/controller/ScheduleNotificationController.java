package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleNotificationDto;
import com.chpark.calendar.exception.ValidGroup;
import com.chpark.calendar.service.ScheduleNotificationService;
import com.chpark.calendar.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
@Slf4j
public class ScheduleNotificationController {

    private final ScheduleNotificationService scheduleNotificationService;

    public ScheduleNotificationController(ScheduleNotificationService scheduleNotificationService) {
        this.scheduleNotificationService = scheduleNotificationService;
    }

    @PostMapping
    public ResponseEntity<List<ScheduleNotificationDto>> createNotification(@RequestParam("schedule-id") long scheduleId,
                                                                            @Validated @RequestBody List<ScheduleNotificationDto> notifications) {
        List<ScheduleNotificationDto> createdNotifications = scheduleNotificationService.create(scheduleId, notifications);

        if(createdNotifications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotifications);
    }

    @GetMapping
    public ResponseEntity<List<ScheduleNotificationDto>> getNotifications(@RequestParam("schedule-id") long scheduleId) {
        List<ScheduleNotificationDto> findResponses = scheduleNotificationService.findByScheduleId(scheduleId);

        return ResponseEntity.ok().body(findResponses);
    }

    @PutMapping
    public ResponseEntity<List<ScheduleNotificationDto>> updateNotification(@RequestParam("schedule-id") long scheduleId,
                                                                            @Validated @RequestBody List<ScheduleNotificationDto> notificationDto) {
        List<ScheduleNotificationDto> responseDto = scheduleNotificationService.update(scheduleId, notificationDto);

        return ResponseEntity.ok().body(responseDto);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteByScheduleId(@RequestParam("schedule-id") long scheduleId) {
        scheduleNotificationService.deleteByScheduleId(scheduleId);

        return ResponseEntity.ok().body("Notifications deleted successfully");
    }
}
