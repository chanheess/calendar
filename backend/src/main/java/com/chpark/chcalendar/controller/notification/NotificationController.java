package com.chpark.chcalendar.controller.notification;

import com.chpark.chcalendar.dto.notification.NotificationDto;
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

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        NotificationService notification =  notificationService.get(NotificationCategory.GROUP);
        List<NotificationDto> notifications = notification.getNotifications(userId);

        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/notifications/groups/{group-id}/invite")
    public ResponseEntity<String> sendGroupInviteNotification(@NotNull @PathVariable("group-id") Long groupId,
                                                              @RequestParam(value = "nickname", required = false) String nickname,
                                                              HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        NotificationService notification =  notificationService.get(NotificationCategory.GROUP);
        notification.sendInviteNotification(userId, groupId, nickname);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications/accept")
    public ResponseEntity<String> acceptNotification(@Validated @RequestBody NotificationDto notificationDto,
                                                              HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        NotificationService notification =  notificationService.get(notificationDto.getCategory());
        notification.acceptNotificationByCategory(userId, notificationDto);

        return ResponseEntity.ok("accepted.");
    }

    @DeleteMapping("/notifications/reject")
    public ResponseEntity<String> rejectNotification(@Validated @RequestBody NotificationDto notificationDto,
                                                     HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        NotificationService notification =  notificationService.get(notificationDto.getCategory());
        notification.deleteNotification(userId, notificationDto);

        return ResponseEntity.ok().build();
    }


}
