package com.chpark.chcalendar.dto.notification;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuatzNotificationDto {
    @NotNull
    private String jobId;

    @NotNull
    private String endpoint;

    @NotNull
    private String payload;

    @NotNull
    private LocalDateTime notificationTime;

    @NotNull
    private String vapidPublicKey;

    @NotNull
    private String vapidPrivateKey;

    @NotNull
    private String vapidSubject;

    public static String createPayload(String title, LocalDateTime body, String jobId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h시 mm분", Locale.KOREAN);
        String formattedTime = body.format(formatter);

        return "{"
                + "\"title\": \"" + title + "\","
                + "\"body\": \"" + formattedTime + "\","
                + "\"icon\": \"https://localhost/images/default-icon.png\","
                + "\"url\": \"https://localhost/" + jobId + "\","
                + "\"eventId\": \"" + jobId + "\","
                + "\"data\": {"
                +     "\"action\": \"openEvent\","
                +     "\"extraInfo\": \"추가적인 정보가 있다면 여기에\""
                + "}"
                + "}";
    }

}
