package com.chpark.chcalendar.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FirebaseTokenDto {
    @NotNull
    String firebaseToken;
    @NotNull
    String platformId;
}
