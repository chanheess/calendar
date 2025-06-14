package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarColorDto;
import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.utility.CookieUtility;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GoogleCalendarService implements CalendarService {

    @Override
    public CalendarDto.Response create(long userId, String title) {
        return null;
    }

    @Override
    public List<CalendarDto.Response> findCalendarList(HttpServletRequest request) {
        String accessToken = CookieUtility.getToken(request, JwtTokenType.GOOGLE_ACCESS);
        if (accessToken == null) {
            return List.of();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = "https://www.googleapis.com/calendar/v3/users/me/calendarList";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return parseGoogleCalendarList(response.getBody());
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<CalendarDto.Response> parseGoogleCalendarList(String json) {
        List<CalendarDto.Response> calendarList = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode itemsNode = rootNode.get("items");
            if (itemsNode != null && itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    // Google 캘린더의 고유 ID 사용 (etag의 숫자 부분 추출)
                    String etag = itemNode.path("etag").asText("");
                    long calendarId = 0;

                    if (!etag.isEmpty()) {
                        try {
                            // 따옴표 제거 후 숫자 추출
                            String numericPart = etag.replaceAll("\"", "").replaceAll("[^0-9]", "");
                            if (!numericPart.isEmpty()) {
                                // 숫자가 너무 크면 해시값으로 변환
                                if (numericPart.length() > 15) {
                                    calendarId = Math.abs(numericPart.hashCode());
                                } else {
                                    calendarId = Long.parseLong(numericPart);
                                }
                            }
                        } catch (NumberFormatException e) {
                            // 파싱 실패 시 해시값 사용
                            calendarId = Math.abs(etag.hashCode());
                        }
                    }
                    
                    // 기본 캘린더 ID가 0인 경우 다른 방법으로 고유 ID 생성
                    if (calendarId == 0) {
                        String calendarIdStr = itemNode.path("id").asText("");
                        calendarId = Math.abs(calendarIdStr.hashCode());
                    }
                    
                    calendarList.add(
                            CalendarDto.Response.builder()
                                .id(calendarId)
                                .title(itemNode.path("summary").asText(""))
                                .color(itemNode.path("backgroundColor").asText(""))
                                .category(CalendarCategory.GOOGLE)
                                .build()
                    );
                }
            }
        } catch (Exception e) {
            // parsing error, return empty list
            e.printStackTrace();
        }
        return calendarList;
    }

    @Override
    public CalendarColorDto changeColor(long userId, CalendarColorDto calendarColorDto) {
        return null;
    }
}
