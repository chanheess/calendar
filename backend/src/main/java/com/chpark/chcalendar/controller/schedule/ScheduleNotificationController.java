package com.chpark.chcalendar.controller.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.schedule.ScheduleNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/schedules/{id}/notifications")
@Slf4j
public class ScheduleNotificationController {

    private final ScheduleNotificationService scheduleNotificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<List<ScheduleNotificationDto>> createNotification(@PathVariable("id") long scheduleId,
                                                                            @Validated @RequestBody List<ScheduleNotificationDto> notifications,
                                                                            HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<ScheduleNotificationDto> createdNotifications = scheduleNotificationService.create(userId, scheduleId, notifications);

        if(createdNotifications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotifications);
    }

    @GetMapping
    public ResponseEntity<List<ScheduleNotificationDto>> getNotifications(@PathVariable("id") long scheduleId) {
        List<ScheduleNotificationDto> findResponses = scheduleNotificationService.findByScheduleId(scheduleId);

        return ResponseEntity.ok().body(findResponses);
    }

    @PutMapping
    public ResponseEntity<List<ScheduleNotificationDto>> updateNotification(@PathVariable("id") long scheduleId,
                                                                            @Validated @RequestBody List<ScheduleNotificationDto> notificationDto,
                                                                            HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<ScheduleNotificationDto> responseDto = scheduleNotificationService.update(userId, scheduleId, notificationDto);

        return ResponseEntity.ok().body(responseDto);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteByScheduleId(@RequestParam("id") long scheduleId) {
        scheduleNotificationService.deleteByScheduleId(scheduleId);

        return ResponseEntity.ok().body("Notifications deleted successfully");
    }
}
