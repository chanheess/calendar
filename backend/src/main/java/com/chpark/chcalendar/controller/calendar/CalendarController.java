package com.chpark.chcalendar.controller.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarSettingDto;
import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.repository.calendar.CalendarQueryRepository;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.calendar.CalendarService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class CalendarController {

    private final Map<CalendarCategory, CalendarService> calendarServiceMap;
    private final CalendarRepository calendarRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/calendars")
    public ResponseEntity<List<CalendarDto.Response>> getCalendarList(@RequestParam(value = "category", required = false) CalendarCategory category,
                                                                      HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarService calendar = calendarServiceMap.get(category);

        return ResponseEntity.ok(calendar.findCalendarList(userId));
    }

    @PostMapping("/calendars")
    public ResponseEntity<CalendarDto.Response> createCalendar(@Validated @RequestBody CalendarDto.Request calendarInfoDto,
                                                               HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarService calendar = calendarServiceMap.get(calendarInfoDto.getCategory());

        return ResponseEntity.ok(calendar.create(userId, calendarInfoDto.getTitle()));
    }

    @PatchMapping("/calendars/{calendarId}")
    public ResponseEntity<CalendarSettingDto> updateCalendar(@PathVariable(value = "calendarId") Long calendarId,
                                                              @Validated @RequestBody CalendarSettingDto calendarSettingDto,
                                                              HttpServletRequest request) {
        CalendarService calendar = calendarServiceMap.get(calendarSettingDto.getCategory());
        calendarSettingDto.setCalendarId(calendarId);

        return ResponseEntity.ok(calendar.updateSetting(request, calendarSettingDto));
    }

    @DeleteMapping("/calendars/{calendarId}")
    public ResponseEntity<String> deleteCalendar(@PathVariable(value = "calendarId") Long calendarId,
                                                 HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarEntity calendar = calendarRepository.findById(calendarId).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 캘린더입니다.")
        );

        calendarServiceMap.get(calendar.getCategory()).deleteCalendar(userId, calendarId);

        return ResponseEntity.ok(calendar.getTitle() + " 캘린더에서 탈퇴되었습니다.");
    }
}
