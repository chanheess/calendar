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
            Calendar service = createGoogleCalendarService(googleSchedule.accessToken());

            //구글 이벤트 생성
            Event event = new Event()
                    .setSummary(googleSchedule.title())
                    .setDescription(googleSchedule.description())
                    .setStart(googleSchedule.startAt())
                    .setEnd(googleSchedule.endAt());

            //이벤트 삽입
            Event createdEvent = service.events().insert(googleSchedule.calendarId(), event).execute();

            Optional<ScheduleEntity> localSchedule = scheduleRepository.findById(googleSchedule.localScheduleId());
            if (localSchedule.isPresent()) {
                ScheduleEntity entity = localSchedule.get();
                entity.setProviderId(createdEvent.getId());
                entity.setEtag(createdEvent.getEtag());
                scheduleRepository.save(entity);
            }

        } catch (Exception e) {
            log.error("Failed to create Google Calendar event", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGoogleScheduleUpdate(GoogleScheduleUpdateEvent googleSchedule) {
        try {
            Calendar service = createGoogleCalendarService(googleSchedule.accessToken());

            Event event = new Event()
                    .setSummary(googleSchedule.title())
                    .setDescription(googleSchedule.description())
                    .setStart(googleSchedule.startAt())
                    .setEnd(googleSchedule.endAt());

            Event updatedEvent = service.events()
                    .update(googleSchedule.calendarId(), googleSchedule.scheduleId(), event)
                    .execute();

            //etag 반영
            Optional<ScheduleEntity> localSchedule = scheduleRepository.findByProviderId(googleSchedule.scheduleId());
            if (localSchedule.isPresent()) {
                ScheduleEntity entity = localSchedule.get();
                entity.setEtag(updatedEvent.getEtag());
                scheduleRepository.save(entity);
            }

        } catch (Exception e) {
            log.error("Failed to update Google Calendar event", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGoogleScheduleDelete(GoogleScheduleDeleteEvent googleSchedule) {
        try {
            Calendar service = createGoogleCalendarService(googleSchedule.accessToken());

            service.events()
                    .delete(googleSchedule.calendarId(), googleSchedule.scheduleId())
                    .execute();

            //로컬 일정이 있으면 구글의 값들 삭제
            Optional<ScheduleEntity> localSchedule = scheduleRepository.findByProviderId(googleSchedule.scheduleId());
            if (localSchedule.isPresent()) {
                ScheduleEntity entity = localSchedule.get();
                entity.setProviderId(null);
                entity.setEtag(null);
                scheduleRepository.save(entity);
            }

        } catch (Exception e) {
            log.error("Failed to delete Google Calendar event", e);
        }
    }

    private Calendar createGoogleCalendarService(String accessToken) throws Exception {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));
        HttpRequestInitializer requestInitializer = request -> {
            new HttpCredentialsAdapter(credentials).initialize(request);
            request.setNumberOfRetries(3);
            request.setUnsuccessfulResponseHandler(
                    new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff())
            );
        };
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer
        ).setApplicationName("chcalendar").build();
    }
}
