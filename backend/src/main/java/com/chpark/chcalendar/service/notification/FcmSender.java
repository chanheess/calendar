package com.chpark.chcalendar.service.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class FcmSender {
    public String send(Message message) throws Exception {
        return FirebaseMessaging.getInstance().send(message);
    }
}
