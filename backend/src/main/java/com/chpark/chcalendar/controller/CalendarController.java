package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.enumClass.CalendarCategory;
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
@RestController
public class CalendarController {

    private final Map<CalendarCategory, CalendarService> calendarService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/calendars")
    public ResponseEntity<List<CalendarInfoDto.Response>> getCalendarList(@RequestParam(value = "category", required = false) CalendarCategory category,
                                                           HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarService calendar = calendarService.get(category);

        return ResponseEntity.ok(calendar.findCalendarList(userId));
    }

    @PostMapping("/calendars")
    public ResponseEntity<CalendarInfoDto.Response> createCalendar(@Validated @RequestBody CalendarInfoDto.Request calendarInfoDto,
                                                                   HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarService calendar = calendarService.get(calendarInfoDto.getCategory());

        return ResponseEntity.ok(calendar.create(userId, calendarInfoDto.getTitle()));
    }
}
