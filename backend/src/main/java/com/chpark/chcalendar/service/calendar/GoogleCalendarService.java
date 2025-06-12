package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarColorDto;
import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.utility.CookieUtility;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GoogleCalendarService implements CalendarService {

    @Override
    public CalendarInfoDto.Response create(long userId, String title) {
        return null;
    }

    @Override
    public List<CalendarInfoDto.Response> findCalendarList(HttpServletRequest request) {
        String accessToken = CookieUtility.getToken(request, JwtTokenType.GOOGLE_ACCESS);
        if (accessToken == null) {
            throw new OAuth2AuthenticationException("access token");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = "https://www.googleapis.com/calendar/v3/users/me/calendarList";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            System.out.println("Response " + response);

            return parseGoogleCalendarList(response.getBody());

        } catch (Exception e) {
            return List.of();
        }
    }

    private List<CalendarInfoDto.Response> parseGoogleCalendarList(String json) {
        List<CalendarInfoDto.Response> calendarList = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode itemsNode = rootNode.get("items");
            if (itemsNode != null && itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    calendarList.add(
                            CalendarInfoDto.Response.builder()
                                .title(itemNode.path("summary").asText(""))
                                .color(itemNode.path("backgroundColor").asText(""))
                                .category(CalendarCategory.GOOGLE)
                                .build()
                    );
                }
            }
        } catch (Exception e) {
            // parsing error, return empty list
        }
        return calendarList;
    }

    @Override
    public CalendarColorDto changeColor(long userId, CalendarColorDto calendarColorDto) {
        return null;
    }
}
