package com.chpark.chcalendar.service.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class FcmSender {
    private final ExecutorService executor = Executors.newFixedThreadPool(50, r -> {
        Thread t = new Thread(r);
        t.setName("fcm-sender-" + t.getId());
        t.setDaemon(true);
        return t;
    });

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    public String send(Message message) throws Exception {
        return FirebaseMessaging.getInstance().send(message);
    }

    public CompletableFuture<String> sendAsync(Message message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return FirebaseMessaging.getInstance().send(message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
}
