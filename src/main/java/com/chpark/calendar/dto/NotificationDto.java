package com.chpark.calendar.dto;

import com.chpark.calendar.entity.NotificationEntity;
import com.chpark.calendar.enumClass.NotificationCategory;
import com.chpark.calendar.enumClass.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    @NotNull
    NotificationCategory category;

    @NotNull
    Long categoryId;

    @NotNull
    NotificationType type;

    @NotNull
    Long typeId;

    String message;

    public NotificationDto(NotificationEntity notification) {
        this.category = notification.getCategory();
        this.categoryId = notification.getCategoryId();
        this.type = notification.getType();
        this.typeId = notification.getTypeId();
        this.message = notification.getMessage();
    }


}
