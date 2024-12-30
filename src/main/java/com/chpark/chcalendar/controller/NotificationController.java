package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.dto.NotificationDto;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.notification.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> getNotifications(HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<NotificationDto> notifications = notificationService.getNotifications(userId);

        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/notifications/groups/{group-id}/invite")
    public ResponseEntity<String> sendInviteNotification(@NotNull @PathVariable("group-id") Long groupId,
                                                         @RequestParam(value = "nickname", required = false) String nickname,
                                                         HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        notificationService.sendGroupInviteNotification(userId, groupId, nickname);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications/accept")
    public ResponseEntity<String> acceptNotification(@Validated @RequestBody NotificationDto notificationDto,
                                                              HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        notificationService.acceptNotificationByCategory(userId, notificationDto);

        return ResponseEntity.ok("accepted.");
    }

    @DeleteMapping("/notifications/reject")
    public ResponseEntity<String> rejectNotification(@Validated @RequestBody NotificationDto notificationDto,
                                                     HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        notificationService.deleteNotification(userId, notificationDto);

        return ResponseEntity.ok().build();
    }
}
