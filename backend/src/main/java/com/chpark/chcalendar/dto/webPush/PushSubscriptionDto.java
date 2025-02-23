package com.chpark.chcalendar.dto.webPush;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PushSubscriptionDto {
    @NotNull
    private String endpoint;

    @NotNull
    private String p256dhKey;

    @NotNull
    private String authKey;
}
