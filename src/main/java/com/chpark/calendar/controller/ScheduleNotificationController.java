package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleNotificationDto;
import com.chpark.calendar.service.ScheduleNotificationService;
import com.chpark.calendar.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
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
            Optional<ScheduleNotificationDto.Response> createResponse = scheduleNotificationService.create(scheduleId, notificationDto);

            if(createResponse.isPresent()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(createResponse.get());
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping
    public ResponseEntity<List<ScheduleNotificationDto.Response>> getNotifications(@RequestParam("scheduleId") int scheduleId) {

        List<ScheduleNotificationDto.Response> findResponses = scheduleNotificationService.findByScheduleId(scheduleId);

        if(findResponses.isEmpty()) {
            ResponseEntity.status(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.status(HttpStatus.OK).body(findResponses);
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<ScheduleNotificationDto.Response> updateNotification(@PathVariable("notificationId") int notificationId,
                                                                               @RequestBody @Valid ScheduleNotificationDto.Request notificationDto) {
        Optional<ScheduleNotificationDto.Response> responseDto = scheduleNotificationService.update(notificationId, notificationDto);

        return ResponseEntity.of(responseDto);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteById(@PathVariable("notificationId") int notificationId) {
        if(scheduleNotificationService.existsById(notificationId)) {
            scheduleNotificationService.deleteById(notificationId);
            return ResponseEntity.status(HttpStatus.OK).body("Notification deleted successfully");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteByScheduleId(@RequestParam("scheduleId") int scheduleId) {
        if(scheduleNotificationService.existsByScheduleId(scheduleId)){
            scheduleNotificationService.deleteByScheduleId(scheduleId);
            return ResponseEntity.status(HttpStatus.OK).body("Notifications deleted successfully");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
    }
}
