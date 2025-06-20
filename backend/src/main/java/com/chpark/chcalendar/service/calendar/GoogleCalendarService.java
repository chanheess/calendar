package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.dto.calendar.CalendarSettingDto;
import com.chpark.chcalendar.entity.calendar.CalendarProviderEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.repository.calendar.CalendarProviderRepository;
import com.chpark.chcalendar.repository.calendar.CalendarQueryRepository;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.calendar.CalendarSettingRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.utility.CookieUtility;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleCalendarService extends CalendarService {

    private final JwtTokenProvider jwtTokenProvider;
    private final CalendarQueryRepository calendarQueryRepository;
    private final CalendarProviderRepository calendarExternalRepository;
    private final RestClient restClient;

    public GoogleCalendarService(CalendarRepository calendarRepository, CalendarSettingRepository calendarSettingRepository, JwtTokenProvider jwtTokenProvider, JwtTokenProvider jwtTokenProvider1, CalendarQueryRepository calendarQueryRepository, CalendarProviderRepository calendarExternalRepository, RestClient restClient) {
        super(calendarRepository, calendarSettingRepository, jwtTokenProvider);
        this.jwtTokenProvider = jwtTokenProvider1;
        this.calendarQueryRepository = calendarQueryRepository;
        this.calendarExternalRepository = calendarExternalRepository;
        this.restClient = restClient;
    }

    @Override
    public CalendarDto.Response create(long userId, String title) {
        return null;
    }

    @Override
    public List<CalendarDto.Response> findCalendarList(long userId) {
        return calendarQueryRepository.findExternalCalendarsByUserId(userId, CalendarCategory.GOOGLE);
    }

    @Override
    public List<Long> findCalendarIdList(long userId) {
        return List.of();
    }

    @Override
    public CalendarSettingDto updateSetting(HttpServletRequest request, CalendarSettingDto calendarSettingDto) {
        String googleAccessToken = CookieUtility.getToken(request, JwtTokenType.GOOGLE_ACCESS);

        if (calendarSettingDto.getTitle() != null) {
            updateGoogleCalendarTitle(googleAccessToken, calendarSettingDto);
        }

        return super.updateSetting(request, calendarSettingDto);
    }

    @Transactional
    public void updateGoogleCalendarTitle(String googleAccessToken, CalendarSettingDto calendarSettingDto) {
        if (googleAccessToken == null || googleAccessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Google Access Token이 필요합니다.");
        }

        CalendarProviderEntity calendarProviderEntity = calendarExternalRepository.findByCalendarId(calendarSettingDto.getCalendarId())
                .orElseThrow(() -> new EntityNotFoundException("캘린더를 찾을 수 없습니다."));

        Map<String, Object> body = new HashMap<>();
        body.put("summary", calendarSettingDto.getTitle());

        try {
            String response = restClient.patch()
                    .uri("https://www.googleapis.com/calendar/v3/calendars/{calendarId}",
                         calendarProviderEntity.getProviderId())
                    .header("Authorization", "Bearer " + googleAccessToken)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, 
                             (request, clientResponse) -> {
                                 throw new RuntimeException("Google Calendar API 클라이언트 오류: " + clientResponse.getStatusCode());
                             })
                    .onStatus(HttpStatusCode::is5xxServerError, 
                             (request, serverResponse) -> {
                                 throw new RuntimeException("Google Calendar API 서버 오류: " + serverResponse.getStatusCode());
                             })
                    .body(String.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Google Calendar 업데이트에 실패했습니다.", e);
        }
    }
}
