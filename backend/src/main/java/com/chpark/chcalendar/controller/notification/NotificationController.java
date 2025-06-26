package com.chpark.chcalendar.controller.notification;

import com.chpark.chcalendar.dto.notification.NotificationDto;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.enumClass.NotificationCategory;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.notification.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class NotificationController {

    private final Map<NotificationCategory, NotificationService> notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> getNotifications(HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        NotificationService notification =  notificationService.get(NotificationCategory.GROUP);
        List<NotificationDto> notifications = notification.getNotifications(userId);

        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/notifications/calendars/{calendar-id}/invite")
    public ResponseEntity<String> sendGroupInviteNotification(@NotNull @PathVariable("calendar-id") Long calendarId,
                                                              @RequestParam(value = "nickname", required = false) String nickname,
                                                              HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        NotificationService notification =  notificationService.get(NotificationCategory.GROUP);
        notification.sendInviteNotification(userId, calendarId, NotificationCategory.GROUP, nickname);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications/accept")
    public ResponseEntity<String> acceptNotification(@Validated @RequestBody NotificationDto notificationDto,
                                                              HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        NotificationService notification =  notificationService.get(notificationDto.getCategory());
        notification.acceptNotificationByCategory(userId, notificationDto);

        return ResponseEntity.ok("accepted.");
    }

    @DeleteMapping("/notifications/reject")
    public ResponseEntity<String> rejectNotification(@Validated @RequestBody NotificationDto notificationDto,
                                                     HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        NotificationService notification =  notificationService.get(notificationDto.getCategory());
        notification.rejectNotificationByCategory(userId, notificationDto);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications/maybe")
    public ResponseEntity<String> maybeNotification(@Validated @RequestBody NotificationDto notificationDto,
                                                     HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        NotificationService notification =  notificationService.get(notificationDto.getCategory());
        notification.pendingNotificationByCategory(userId, notificationDto);

        return ResponseEntity.ok("accepted.");
    }


}
