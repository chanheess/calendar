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
    private final ScheduleService scheduleService;

    public ScheduleNotificationController(ScheduleNotificationService scheduleNotificationService,
                                          ScheduleService scheduleService) {
        this.scheduleNotificationService = scheduleNotificationService;
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<List<ScheduleNotificationDto>> createNotification(@RequestParam("schedule-id") int scheduleId,
                                                                            @Validated @RequestBody List<ScheduleNotificationDto> notifications) {
        if(scheduleService.existsById(scheduleId)) {
            List<ScheduleNotificationDto> createResponse = scheduleNotificationService.create(scheduleId, notifications);

            if(!createResponse.isEmpty()) {
                return new ResponseEntity<>(createResponse, HttpStatus.CREATED);
            }
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping
    public ResponseEntity<List<ScheduleNotificationDto>> getNotifications(@RequestParam("schedule-id") int scheduleId) {
        List<ScheduleNotificationDto> findResponses = scheduleNotificationService.findByScheduleId(scheduleId);

        return ResponseEntity.status(HttpStatus.OK).body(findResponses);
    }

    @PutMapping
    public ResponseEntity<List<ScheduleNotificationDto>> updateNotification(@RequestParam("schedule-id") int scheduleId,
                                                                            @Validated @RequestBody List<ScheduleNotificationDto> notificationDto) {
        List<ScheduleNotificationDto> responseDto = scheduleNotificationService.update(scheduleId, notificationDto);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteByScheduleId(@RequestParam("schedule-id") int scheduleId) {
        if(scheduleNotificationService.existsByScheduleId(scheduleId)){
            scheduleNotificationService.deleteByScheduleId(scheduleId);
            return ResponseEntity.status(HttpStatus.OK).body("Notifications deleted successfully");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
    }
}
