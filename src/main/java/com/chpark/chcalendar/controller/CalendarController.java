package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.dto.calendar.CalendarListDto;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.calendar.GroupCalendarService;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class CalendarController {

    private final UserCalendarService userCalendarService;
    private final GroupCalendarService groupCalendarService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/calendars")
    public ResponseEntity<CalendarListDto> getCalendarList(HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<CalendarInfoDto> groupList = groupCalendarService.findCalendarList(userId);
        List<CalendarInfoDto> userList = userCalendarService.findCalendarList(userId);

        return ResponseEntity.ok(new CalendarListDto(groupList, userList));
    }

//    @GetMapping("/calendars/schedule")
//    public ResponseEntity<>
}
