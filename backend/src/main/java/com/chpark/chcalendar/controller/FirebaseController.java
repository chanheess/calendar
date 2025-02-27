package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.notification.FirebaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class FirebaseController {

    private final JwtTokenProvider jwtTokenProvider;
    private final FirebaseService firebaseService;

    @PostMapping("/notifications/token/{fcmToken}")
    public ResponseEntity<String> subscribe(@NotNull @PathVariable("fcmToken") String fcmToken,
                                            HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        firebaseService.saveToken(userId, fcmToken);

        return ResponseEntity.ok("Subscription successful");
    }

    @DeleteMapping("/notifications/token/{fcmToken}")
    public ResponseEntity<String> unsubscribe(@Validated @PathVariable("fcmToken") String fcmToken,
                                              HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        firebaseService.deleteToken(userId, fcmToken);

        return ResponseEntity.ok("Unsubscription successful");
    }
}
