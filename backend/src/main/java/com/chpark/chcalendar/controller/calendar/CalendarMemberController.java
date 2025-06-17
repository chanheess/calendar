package com.chpark.chcalendar.controller.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarMemberDto;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.calendar.CalendarMemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class CalendarMemberController {

    private final CalendarMemberService calendarMemberService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/groups/{calendar-id}/users")
    public ResponseEntity<List<CalendarMemberDto>> getUsers(@NotNull @PathVariable("calendar-id") Long calendarId,
                                                            HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<CalendarMemberDto> result = calendarMemberService.findCalendarMemberList(userId, calendarId);

        return ResponseEntity.ok(result);
    }
}
