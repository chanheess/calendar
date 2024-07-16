package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleNotificationDto;
import com.chpark.calendar.service.ScheduleNotificationService;
import com.chpark.calendar.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
@Slf4j
public class ScheduleNotificationController {

    private final ScheduleNotificationService scheduleNotificationService;
    private final ScheduleService scheduleService;

    public ScheduleNotificationController(ScheduleNotificationService scheduleNotificationService,
                                          ScheduleService scheduleService) {
        this.scheduleNotificationService = scheduleNotificationService;
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<ScheduleNotificationDto.Response> createNotification(@RequestParam("scheduleId") int scheduleId,
                                                                               @RequestBody @Valid ScheduleNotificationDto.Request notificationDto) {

        if(scheduleService.existsById(scheduleId)) {
            return ResponseEntity.of(scheduleNotificationService.create(scheduleId, notificationDto));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    @GetMapping
    public List<ScheduleNotificationDto.Response> getNotifications(@RequestParam("scheduleId") int scheduleId) {
        return scheduleNotificationService.findByScheduleId(scheduleId);
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<ScheduleNotificationDto.Response> updateNotification(@PathVariable("notificationId") int notificationId,
                                                                               @RequestBody @Valid ScheduleNotificationDto.Request notificationDto) {
        Optional<ScheduleNotificationDto.Response> responseDto = scheduleNotificationService.update(notificationId, notificationDto);

        return ResponseEntity.of(responseDto);
    }

    @DeleteMapping("/{notificationId}")
    public void deleteNotification(@PathVariable("notificationId") int notificationId) {

        scheduleNotificationService.deleteById(notificationId);
    }

    @DeleteMapping
    public void deleteNotifications(@RequestParam("scheduleId") int scheduleId) {
        scheduleNotificationService.deleteNotifications(scheduleId);
    }

}
