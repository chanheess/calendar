package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.dto.webPush.PushSubscriptionDto;
import com.chpark.chcalendar.dto.webPush.PushUnsubscriptionDto;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.WebPushService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class WebPushController {

    private final JwtTokenProvider jwtTokenProvider;
    private final WebPushService webPushService;

    @Value("${vapid.publicKey}")
    private String vapidPublicKey;

    @Value("${vapid.privateKey}")
    private String vapidPrivateKey;

    @PostMapping("/web-push/subscribe")
    public ResponseEntity<String> subscribe(@Validated @RequestBody PushSubscriptionDto subscription,
                                            HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        webPushService.pushSubscription(userId, subscription);

        return ResponseEntity.ok("Subscription successful");
    }

    @DeleteMapping("/web-push/subscribe")
    public ResponseEntity<String> unsubscribe(@Validated @RequestBody PushUnsubscriptionDto unsubscription,
                                              HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        webPushService.pushUnsubscription(userId, unsubscription);

        return ResponseEntity.ok("Unsubscription successful");
    }

    @GetMapping("/web-push/vapidPublicKey")
    public String getVapidPublicKey() {
        return vapidPublicKey;
    }
}
