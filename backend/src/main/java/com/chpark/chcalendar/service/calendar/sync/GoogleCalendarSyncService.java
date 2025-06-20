package com.chpark.chcalendar.service.calendar.sync;

import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.calendar.CalendarProviderEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.repository.calendar.CalendarQueryRepository;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class GoogleCalendarSyncService implements CalendarSyncService {

    private final JwtTokenProvider jwtTokenProvider;
    private final CalendarQueryRepository calendarQueryRepository;
    private final CalendarRepository calendarRepository;

    @Override
    public void syncCalendars(String accessToken, HttpServletRequest request) {
        if (accessToken == null) {
            return;
        }

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = "https://www.googleapis.com/calendar/v3/users/me/calendarList";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;

        response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Map<String, CalendarEntity> googleCalendarList = parseGoogleCalendarList(response.getBody(), userId);
        Map<String, CalendarEntity> localCalendarList = calendarQueryRepository.findExternalCalendar(userId, CalendarCategory.GOOGLE);

        //로컬 캘린더와 동기화 처리
        for (Map.Entry<String, CalendarEntity> entry : googleCalendarList.entrySet()) {
            String externalId = entry.getKey();
            CalendarEntity googleCalendar = entry.getValue();

            if (!localCalendarList.containsKey(externalId)) {
                calendarRepository.save(googleCalendar);
            } else {
                CalendarEntity localCalendar = localCalendarList.get(externalId);
                if (!Objects.equals(googleCalendar.getTitle(), localCalendar.getTitle())) {
                    updateCalendar(localCalendar, googleCalendar);
                }
            }
        }
    }

    private Map<String, CalendarEntity> parseGoogleCalendarList(String json, long userId) {
        Map<String, CalendarEntity> calendarList = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode itemsNode = rootNode.get("items");

            if (itemsNode != null && itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    String googleCalendarId = itemNode.path("id").asText("");
                    String title = itemNode.path("summary").asText("");
                    String color = itemNode.path("backgroundColor").asText("");


                    CalendarEntity calendarEntity = CalendarEntity.builder()
                            .title(title)
                            .userId(userId)
                            .category(CalendarCategory.GOOGLE)
                            .build();

                    CalendarProviderEntity calendarProviderEntity = CalendarProviderEntity.builder()
                            .calendar(calendarEntity)
                            .providerId(googleCalendarId)
                            .provider("google")
                            .build();

                    if (!calendarEntity.getCalendarSettings().isEmpty()) {
                        calendarEntity.getCalendarSettings().get(0).setColor(color);
                    }
                    calendarEntity.setCalendarProvider(calendarProviderEntity);

                    calendarList.put(googleCalendarId, calendarEntity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendarList;
    }

    public void updateCalendar(CalendarEntity localCalendar, CalendarEntity googleCalendar) {
        if (!localCalendar.getTitle().equals(googleCalendar.getTitle())) {
            localCalendar.setTitle(googleCalendar.getTitle());
        }

        calendarRepository.save(localCalendar);
    }
}
