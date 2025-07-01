package com.chpark.chcalendar.event.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class GoogleScheduleEventListener {

    private final ScheduleRepository scheduleRepository;
    private static final Logger log = LoggerFactory.getLogger(GoogleScheduleEventListener.class);

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGoogleScheduleCreate(GoogleScheduleCreateEvent googleSchedule) {
        try {
            //구글 인증 객체 생성 (최신 방식)
            GoogleCredentials credentials = GoogleCredentials.create(
                    new AccessToken(googleSchedule.accessToken(), null)
            );

            //Calendar 서비스 생성 (재시도, BackOff 포함)
            HttpRequestInitializer requestInitializer = request -> {
                new HttpCredentialsAdapter(credentials).initialize(request);
                request.setNumberOfRetries(3); // 3회 재시도
                request.setUnsuccessfulResponseHandler(
                        new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff())
                );
            };

            Calendar service = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer
            ).setApplicationName("chcalendar").build();

            //구글 이벤트 생성
            Event event = new Event()
                    .setSummary(googleSchedule.title())
                    .setDescription(googleSchedule.description())
                    .setStart(googleSchedule.startAt())
                    .setEnd(googleSchedule.startAt());

            //이벤트 삽입 (자동 재시도)
            Event createdEvent = service.events().insert(googleSchedule.calendarId(), event).execute();

            Optional<ScheduleEntity> localSchedule = scheduleRepository.findById(googleSchedule.localScheduleId());
            if (localSchedule.isPresent()) {
                ScheduleEntity entity = localSchedule.get();
                entity.setProviderId(createdEvent.getId());
                entity.setEtag(createdEvent.getEtag());
                scheduleRepository.save(entity);
            }

        } catch (Exception e) {
            log.error("Failed", e);
        }
    }
}
