package com.chpark.chcalendar.service.schedule.sync;

import com.chpark.chcalendar.dto.CursorPage;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.exception.authentication.TokenAuthenticationException;
import com.chpark.chcalendar.repository.calendar.CalendarQueryRepository;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleQueryRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

@RequiredArgsConstructor
@Service
public class GoogleScheduleSyncService implements ScheduleSyncService{

    private final JwtTokenProvider jwtTokenProvider;

    private final CalendarQueryRepository calendarQueryRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;

    private final CalendarRepository calendarRepository;
    private final ScheduleRepository scheduleRepository;

    @Value("${GOOGLE_API_KEY}")
    private String googleAPIKey;

    @Transactional
    public void syncSchedules(String accessToken, HttpServletRequest request) {
        if (accessToken == null) return;

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<CalendarEntity> calendarList = calendarQueryRepository.findSyncExternalCalendarsByUserId(userId, CalendarCategory.GOOGLE);

        List<ScheduleEntity> localSchedules = getLocalSchedule(calendarList, userId, 100);

        calendarList.forEach(calendarEntity -> {
            boolean checked = calendarEntity.getCalendarSettings().stream()
                    .anyMatch(setting -> setting.getUserId().equals(userId));

            if (!checked) return;

            try {
                List<ScheduleEntity> googleSchedules = pageGoogleSchedule(calendarEntity, accessToken);

                googleSchedules.forEach(googleSchedule -> {
                    Optional<ScheduleEntity> localOpt = localSchedules.stream()
                            .filter(local -> googleSchedule.getProviderId().equals(local.getProviderId()))
                            .findFirst();

                    //create
                    if (localOpt.isEmpty()) {
                        scheduleRepository.save(googleSchedule);
                        return;
                    }

                    //no update
                    ScheduleEntity localSchedule = localOpt.get();
                    if (Objects.equals(localSchedule.getEtag(), googleSchedule.getEtag())) {
                        return;
                    }

                    LocalDateTime googleUpdated = googleSchedule.getUpdatedAt();
                    LocalDateTime localUpdated = localSchedule.getUpdatedAt();

                    //overwrite
                    if (googleUpdated != null && (localUpdated == null || googleUpdated.isAfter(localUpdated))) {
                        // 구글이 최신 -> 덮어쓰기
                        localSchedule.overwrite(googleSchedule);
                        scheduleRepository.save(localSchedule);
                    } else {
                        // 로컬이 최신 -> 양방향 동기화
                        pushLocalScheduleToGoogle(localSchedule, accessToken, calendarEntity.getCalendarProvider().getProviderId());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Transactional
    private List<ScheduleEntity> pageGoogleSchedule(CalendarEntity calendarEntity, String accessToken) throws IOException {
        List<ScheduleEntity> googleSchedules = new ArrayList<>();
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

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            googleSchedules.addAll(parseGoogleScheduleList(rootNode, calendarEntity));

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

    private List<ScheduleEntity> parseGoogleScheduleList(JsonNode rootNode, CalendarEntity calendarEntity) {
        List<ScheduleEntity> scheduleList = new ArrayList<>();
        JsonNode itemsNode = rootNode.path("items");

        if (itemsNode != null && itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                if ("cancelled".equals(itemNode.path("status").asText())) {
                    continue;
                }
                String id = itemNode.path("id").asText("");
                String title = itemNode.path("summary").asText("");
                String description = itemNode.path("description").asText("");
                LocalDateTime startAt = parseGoogleDateToKST(itemNode, "start");
                LocalDateTime endAt = parseGoogleDateToKST(itemNode, "end");
                String etag = itemNode.path("etag").asText("");
                LocalDateTime createdAt = parseToLocalDateTime(itemNode.path("created").asText(""));
                LocalDateTime updatedAt = parseToLocalDateTime(itemNode.path("updated").asText(""));

                ScheduleEntity scheduleEntity = ScheduleEntity.builder()
                        .title(title)
                        .description(description)
                        .startAt(startAt)
                        .endAt(endAt)
                        .userId(calendarEntity.getUserId())
                        .calendarId(calendarEntity.getId())
                        .providerId(id)
                        .etag(etag)
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();

                scheduleList.add(scheduleEntity);
            }
        }
        return scheduleList;
    }

    private LocalDateTime parseToLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return OffsetDateTime.parse(dateTimeStr).toLocalDateTime();
    }

    private LocalDateTime parseGoogleDateToKST(JsonNode itemNode, String nodeName) {
        // dateTime 우선 시도
        String dateTimeStr = itemNode.path(nodeName).path("dateTime").asText("");
        if (dateTimeStr != null && !dateTimeStr.isEmpty()) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(dateTimeStr);
                // 항상 KST로 변환
                return odt.atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
            } catch (Exception e) {
                // 무시하고 아래로
            }
        }

        // 만약 dateTime 파싱이 실패하면 date(종일 일정) 시도
        String dateStr = itemNode.path(nodeName).path("date").asText("");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(dateStr);
                // KST로 00:00:00 변환
                return date.atStartOfDay(ZoneId.of("Asia/Seoul")).toLocalDateTime();
            } catch (Exception e) {
                // 무시
            }
        }

        // 아무것도 없으면 null
        return null;
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

    @Transactional
    public void pushLocalScheduleToGoogle(ScheduleEntity localSchedule, String accessToken, String googleCalendarId) {
        String eventId = localSchedule.getProviderId();

        String url = "https://www.googleapis.com/calendar/v3/calendars/" + googleCalendarId + "/events/" + eventId;

        //JSON으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> googleEvent = new HashMap<>();
        googleEvent.put("summary", localSchedule.getTitle());
        googleEvent.put("description", localSchedule.getDescription());
        googleEvent.put("start", Map.of("dateTime", localSchedule.getStartAt().toString()));
        googleEvent.put("end", Map.of("dateTime", localSchedule.getEndAt().toString()));

        try {
            String requestBody = objectMapper.writeValueAsString(googleEvent);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PATCH, requestEntity, String.class
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
