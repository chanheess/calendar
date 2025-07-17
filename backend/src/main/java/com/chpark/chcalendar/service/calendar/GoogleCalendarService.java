package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.dto.calendar.CalendarSettingDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.calendar.CalendarProviderEntity;
import com.chpark.chcalendar.enumClass.CRUDAction;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.exception.authorization.CalendarAuthorizationException;
import com.chpark.chcalendar.repository.calendar.CalendarProviderRepository;
import com.chpark.chcalendar.repository.calendar.CalendarQueryRepository;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.calendar.CalendarSettingRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.utility.CookieUtility;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class GoogleCalendarService extends CalendarService {

    private final CalendarQueryRepository calendarQueryRepository;
    private final CalendarProviderRepository calendarProviderRepository;
    private final RestClient restClient;

    public GoogleCalendarService(CalendarRepository calendarRepository, CalendarSettingRepository calendarSettingRepository, JwtTokenProvider jwtTokenProvider, ApplicationEventPublisher eventPublisher, CalendarQueryRepository calendarQueryRepository, CalendarProviderRepository calendarProviderRepository, RestClient restClient) {
        super(calendarRepository, calendarSettingRepository, jwtTokenProvider, eventPublisher);
        this.calendarQueryRepository = calendarQueryRepository;
        this.calendarProviderRepository = calendarProviderRepository;
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
        return calendarQueryRepository.findCalendarIdByUserId(userId, CalendarCategory.GOOGLE);
    }

    @Override
    public void checkAuthority(CRUDAction action, long userId, long calendarId) {
        CalendarEntity calendarEntity = calendarRepository.findByIdAndUserId(calendarId, userId).orElseThrow(
                () -> new EntityNotFoundException("접근 권한이 없습니다.")
        );

        if (calendarEntity.getCalendarProvider() == null) {
            throw new EntityNotFoundException("접근 권한이 없습니다.");
        }

        if (Objects.equals(calendarEntity.getCalendarProvider().getStatus(), "reader") && !action.equals(CRUDAction.READ)) {
            throw new CalendarAuthorizationException("읽기 권한만 있습니다. (수정/삭제/생성 불가)");
        }
    }

    @Override
    public void deleteCalendar(long userId, long calendarId) {
        Optional<CalendarEntity> calendarEntity = calendarRepository.findByIdAndUserId(calendarId, userId);

        if (calendarEntity.isEmpty()) {
            return;
        }

        calendarSettingRepository.deleteAll(calendarEntity.get().getCalendarSettings());
        calendarProviderRepository.deleteByCalendarId(calendarId);
        calendarRepository.delete(calendarEntity.get());
    }

    @Override
    public CalendarSettingDto updateSetting(HttpServletRequest request, CalendarSettingDto calendarSettingDto) {
        String googleAccessToken = CookieUtility.getToken(request, JwtTokenType.GOOGLE_ACCESS);

        if (googleAccessToken == null || googleAccessToken.isEmpty()) {
            throw new IllegalStateException("Google 액세스 토큰이 없습니다. 다시 로그인해주세요.");
        }

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

        CalendarProviderEntity calendarProviderEntity = calendarProviderRepository.findByCalendarId(calendarSettingDto.getCalendarId())
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
