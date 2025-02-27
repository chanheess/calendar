package com.chpark.chcalendar.entity;

import com.chpark.chcalendar.dto.notification.NotificationDto;
import com.chpark.chcalendar.enumClass.NotificationCategory;
import com.chpark.chcalendar.enumClass.NotificationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "notification")
public class NotificationEntity {

    @Id
    String key;

    NotificationCategory category;
    Long categoryId;

    NotificationType type;
    Long typeId;

    String message;

    @TimeToLive
    private Long expiration;

    public NotificationEntity(Long userId,
                              NotificationCategory category,
                              Long categoryId,
                              NotificationType type,
                              Long typeId,
                              String message,
                              Long expiration) {
        this.key = "user:" + userId + ":" + category + ":" + categoryId + ":" + type + ":" + typeId;
        this.category = category;
        this.categoryId = categoryId;
        this.type = type;
        this.typeId = typeId;
        this.message = message;
        this.expiration = expiration;
    }

    public NotificationEntity(Long userId,
                              NotificationDto notificationDto,
                              Long expiration) {
        this.category = notificationDto.getCategory();
        this.categoryId = notificationDto.getCategoryId();
        this.type = notificationDto.getType();
        this.typeId = notificationDto.getTypeId();
        this.message = notificationDto.getMessage();
        this.expiration = expiration;
        this.key = "user:" + userId + ":" + this.category + ":" + this.categoryId + ":" + this.type + ":" + this.typeId;
    }
}
