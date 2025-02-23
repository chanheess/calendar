package com.chpark.chcalendar.service;

import com.chpark.chcalendar.dto.webPush.PushSubscriptionDto;
import com.chpark.chcalendar.dto.webPush.PushUnsubscriptionDto;
import com.chpark.chcalendar.entity.WebPushEntity;
import com.chpark.chcalendar.repository.WebPushRepository;
import com.chpark.chcalendar.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebPushService {

    private final WebPushRepository webPushRepository;
    private final UserRepository userRepository;

    public void pushSubscription(long userId, PushSubscriptionDto subscription) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found.");
        }

        WebPushEntity pushEntity = new WebPushEntity(
            userId,
            subscription.getEndpoint(),
            subscription.getP256dhKey(),
            subscription.getAuthKey()
        );

        webPushRepository.save(pushEntity);
    }

    public void pushUnsubscription(long userId, PushUnsubscriptionDto unsubscriptionDto) {
        Optional<WebPushEntity> webPush = webPushRepository.findByEndpoint(unsubscriptionDto.getEndpoint());

        if (webPush.isEmpty()) {
            return;
        }

        if (webPush.get().getId() != userId) {
            throw new IllegalArgumentException("접근할 수 없는 유저입니다.");
        }

        webPushRepository.delete(webPush.get());
    }
}
