package com.chpark.chcalendar.service.schedule.sync;

import com.chpark.chcalendar.dto.CursorPage;
import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.GoogleScheduleStatus;
import com.chpark.chcalendar.exception.authentication.TokenAuthenticationException;
import com.chpark.chcalendar.repository.calendar.CalendarQueryRepository;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleQueryRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.service.schedule.ScheduleService;
import com.chpark.chcalendar.utility.ScheduleUtility;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class GoogleScheduleSyncService implements ScheduleSyncService{

    private final CalendarQueryRepository calendarQueryRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;

    private final CalendarRepository calendarRepository;
    private final ScheduleRepository scheduleRepository;

    private final ScheduleService scheduleService;


    private static final Logger log = LoggerFactory.getLogger(GoogleScheduleSyncService.class);

    @Value("${GOOGLE_API_KEY}")
    private String googleAPIKey;

    private final ObjectMapper objectMapper;

    @Transactional
    public void syncSchedules(String accessToken, long userId) {
        if (accessToken == null) return;

        List<CalendarEntity> calendarList = calendarQueryRepository.findSyncExternalCalendarsByUserId(userId, CalendarCategory.GOOGLE);
        List<ScheduleEntity> localSchedules = getLocalSchedule(calendarList, userId, 100);

        calendarList.forEach(calendarEntity -> {
            boolean checked = calendarEntity.getCalendarSettings().stream()
                    .anyMatch(setting -> setting.getUserId().equals(userId));

            if (!checked) return;

            try {
                Map<GoogleScheduleStatus, List<ScheduleDto.Request>> googleSchedules = pageGoogleSchedule(calendarEntity, accessToken);
                saveSchedule(googleSchedules.get(GoogleScheduleStatus.CONFIRMED), localSchedules);
                deleteSchedule(googleSchedules.get(GoogleScheduleStatus.CANCELLED), localSchedules);
            } catch (Exception e) {
                log.error("Failed", e);
            }
        });
    }

    @Transactional
    public void saveSchedule(List<ScheduleDto.Request> googleSchedules, List<ScheduleEntity> localSchedules) {
        if (googleSchedules == null) {
            return;
        }

        googleSchedules.forEach(googleSchedule -> {
            if (googleSchedule.getScheduleDto() == null) {
                return;
            }

            ScheduleDto googleScheduleDto = googleSchedule.getScheduleDto();
            Optional<ScheduleEntity> localOpt = localSchedules.stream()
                    .filter(local -> googleScheduleDto.getProviderId().equals(local.getProviderId()))
                    .findFirst();

            //create
            if (localOpt.isEmpty()) {
                scheduleService.createByForm(googleSchedule, googleScheduleDto.getUserId());
                return;
            }

            //no update
            ScheduleEntity localSchedule = localOpt.get();
            if (Objects.equals(localSchedule.getEtag(), googleScheduleDto.getEtag())) {
                return;
            }

            //overwrite
            scheduleService.updateSchedule(localSchedule.getId(), false, googleSchedule, googleScheduleDto.getUserId());
        });
    }

    @Transactional
    public void deleteSchedule(List<ScheduleDto.Request> googleSchedules, List<ScheduleEntity> localSchedules) {
        if (googleSchedules == null) {
            return;
        }

        googleSchedules.forEach(googleSchedule -> {
            if (googleSchedule.getScheduleDto() == null) {
                return;
            }

            ScheduleDto googleScheduleDto = googleSchedule.getScheduleDto();

            Optional<ScheduleEntity> localOpt = localSchedules.stream()
                    .filter(local -> googleScheduleDto.getProviderId().equals(local.getProviderId()))
                    .findFirst();
            //no delete
            if (localOpt.isEmpty()) {
                return;
            }

            ScheduleEntity localSchedule = localOpt.get();
            if (Objects.equals(localSchedule.getEtag(), googleScheduleDto.getEtag())) {
                return;
            }

            //delete
            scheduleService.deleteById(localSchedule.getId(), localSchedule.getCalendarId(), localSchedule.getUserId());
        });
    }

    @Transactional
    private Map<GoogleScheduleStatus, List<ScheduleDto.Request>> pageGoogleSchedule(CalendarEntity calendarEntity, String accessToken) throws IOException {
        Map<GoogleScheduleStatus, List<ScheduleDto.Request>> googleSchedules = new HashMap<>();
        String pageToken = null;
        String newSyncToken = null;

        do {
            String urlString = "";
            String responseBody = "";
            try {
                urlString = createUrl(calendarEntity, pageToken);
                responseBody = getGoogleSchedule(accessToken, urlString);
            } catch (TokenAuthenticationException ex) {
                //토큰을 만료시켜서 전체 리프레시하도록
                calendarEntity.getCalendarProvider().setSyncToken(null);
                urlString = createUrl(calendarEntity, pageToken);
                responseBody = getGoogleSchedule(accessToken, urlString);
            }

            JsonNode rootNode = objectMapper.readTree(responseBody);

            Map<GoogleScheduleStatus, List<ScheduleDto.Request>> toAdd = parseGoogleScheduleMap(rootNode, calendarEntity);
            for (Map.Entry<GoogleScheduleStatus, List<ScheduleDto.Request>> entry : toAdd.entrySet()) {
                googleSchedules.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
            }

            pageToken = rootNode.has("nextPageToken") ? rootNode.get("nextPageToken").asText(null) : null;

            if (rootNode.has("nextSyncToken")) {
                newSyncToken = rootNode.get("nextSyncToken").asText();
            }
        } while (pageToken != null);

        if (newSyncToken != null) {
             calendarEntity.getCalendarProvider().setSyncToken(newSyncToken);
             calendarRepository.save(calendarEntity);
        }

        return googleSchedules;
    }

    @Transactional
    public List<ScheduleEntity> getLocalSchedule(List<CalendarEntity> calendarList, long userId, int pageSize) {
        List<ScheduleEntity> allEvents = new ArrayList<>();
        String nextCursor = null;

        do {
            LocalDateTime cursorTime = null;
            Long cursorId = null;
            if (nextCursor != null) {
                String[] parts = nextCursor.split("_");
                cursorTime = LocalDateTime.parse(parts[0]);
                cursorId = Long.parseLong(parts[1]);
            }

            // 한 페이지 요청
            CursorPage<ScheduleEntity> page = pageLocalSchedule(userId, calendarList, cursorTime, cursorId, pageSize);

            // 결과 추가
            allEvents.addAll(page.getContent());
            nextCursor = page.getNextCursor();

        } while (nextCursor != null);

        return allEvents;
    }

    @Transactional
    public CursorPage<ScheduleEntity> pageLocalSchedule(
            long userId,
            List<CalendarEntity> calendarList,
            LocalDateTime cursorTime,
            Long cursorId,
            int pageSize
    ) {
        Pageable pageable = PageRequest.of(0, pageSize);

        List<ScheduleEntity> events = scheduleQueryRepository.findSchedulesByCalendarIdAndUser(
                userId, calendarList, cursorTime, cursorId, pageable
        );

        String nextCursor = null;
        if (!events.isEmpty()) {
            ScheduleEntity last = events.get(events.size() - 1);
            nextCursor = last.getStartAt() + "_" + last.getId();
        }

        return new CursorPage<>(
                events,
                nextCursor
        );
    }

    @Transactional
    private String getGoogleSchedule(String accessToken, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = con.getResponseCode();
        if (responseCode >= 400) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
                StringBuilder errorResponse = new StringBuilder();
                String inputLine;
                while ((inputLine = errorReader.readLine()) != null) {
                    errorResponse.append(inputLine);
                }
                if (responseCode == 410 && errorResponse.toString().contains("fullSyncRequired")) {
                    // 예외를 던지거나, 플래그 반환 또는 로깅
                    throw new TokenAuthenticationException("Sync token is no longer valid. Full sync is required.");
                }

                throw new IOException("HTTP error code: " + responseCode + ", response: " + errorResponse.toString());
            }
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }



    private Map<GoogleScheduleStatus, List<ScheduleDto.Request>> parseGoogleScheduleMap(JsonNode rootNode, CalendarEntity calendarEntity) {
        Map<GoogleScheduleStatus, List<ScheduleDto.Request>> scheduleEntityMap = new HashMap<>();
        JsonNode itemsNode = rootNode.path("items");

        if (itemsNode != null && itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                JsonNode recurrenceNode = itemNode.path("recurrence");
                boolean isRepeat = recurrenceNode.isArray() && !recurrenceNode.isEmpty();
                if (isRepeat) {
                    continue;
                }

                GoogleScheduleStatus status = GoogleScheduleStatus.from(itemNode.path("status").asText());

                ScheduleDto.Request result = new ScheduleDto.Request();
                result.setScheduleDto(ScheduleUtility.parseScheduleDto(itemNode, calendarEntity));
                result.setNotificationDto(ScheduleUtility.parseGoogleNotificationDtoList(itemNode, result.getScheduleDto().getStartAt()));

                scheduleEntityMap
                        .computeIfAbsent(status, k -> new ArrayList<>())
                        .add(result);
            }
        }
        return scheduleEntityMap;
    }

    public String createUrl(CalendarEntity calendarEntity, String pageToken) {
        String calendarId = calendarEntity.getCalendarProvider().getProviderId().trim();
        String syncToken = calendarEntity.getCalendarProvider().getSyncToken();
        String status = calendarEntity.getCalendarProvider().getStatus();

        if (status.equals("reader")) {
            calendarId = URLEncoder.encode(calendarId, StandardCharsets.UTF_8);
        }

        StringBuilder urlBuilder = new StringBuilder("https://www.googleapis.com/calendar/v3/calendars/")
                .append(calendarId)
                .append("/events");

        boolean hasParam = false;
        if (syncToken != null && !syncToken.isEmpty()) {
            urlBuilder.append("?syncToken=").append(syncToken);
            hasParam = true;
        } else if (status.equals("reader")) {
            urlBuilder.append("?key=").append(googleAPIKey);
            hasParam = true;
        }

        if (pageToken != null && !pageToken.isEmpty()) {
            urlBuilder.append(hasParam ? "&" : "?").append("pageToken=").append(pageToken);
        }

        return urlBuilder.toString();
    }
}
