package com.chpark.chcalendar.dto.webPush;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PushUnsubscriptionDto {

    @NotNull
    private String endpoint;
}
