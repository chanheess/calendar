package com.chpark.chcalendar.controller.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarSettingDto;
import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.calendar.CalendarService;
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

    private final Map<CalendarCategory, CalendarService> calendarService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/calendars")
    public ResponseEntity<List<CalendarDto.Response>> getCalendarList(@RequestParam(value = "category", required = false) CalendarCategory category,
                                                                      HttpServletRequest request) {
        CalendarService calendar = calendarService.get(category);

        return ResponseEntity.ok(calendar.findCalendarList(request));
    }

    @PostMapping("/calendars")
    public ResponseEntity<CalendarDto.Response> createCalendar(@Validated @RequestBody CalendarDto.Request calendarInfoDto,
                                                               HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarService calendar = calendarService.get(calendarInfoDto.getCategory());

        return ResponseEntity.ok(calendar.create(userId, calendarInfoDto.getTitle()));
    }

    @PatchMapping("/calendars/{calendarId}")
    public ResponseEntity<CalendarSettingDto> updateCalendar(@PathVariable(value = "calendarId") Long calendarId,
                                                                  @Validated @RequestBody CalendarSettingDto calendarSettingDto,
                                                                  HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarService calendar = calendarService.get(calendarSettingDto.getCategory());
        calendarSettingDto.setCalendarId(calendarId);

        return ResponseEntity.ok(calendar.updateSetting(userId, calendarSettingDto));
    }
}
