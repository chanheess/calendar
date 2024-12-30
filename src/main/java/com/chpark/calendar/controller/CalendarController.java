package com.chpark.calendar.controller;

import com.chpark.calendar.dto.CalendarListDto;
import com.chpark.calendar.security.JwtTokenProvider;
import com.chpark.calendar.service.CalendarService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class CalendarController {

    private final CalendarService calendarService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/calendars")
    public ResponseEntity<CalendarListDto.Response> getCalendarList(HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        return ResponseEntity.ok(calendarService.getCalendarList(userId));
    }

    @GetMapping("/calendars/schedule")
    public ResponseEntity<>
}
