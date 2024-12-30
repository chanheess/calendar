package com.chpark.chcalendar.controller.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.chpark.chcalendar.service.schedule.ScheduleNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule-notifications")
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
