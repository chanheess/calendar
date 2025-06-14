package com.chpark.chcalendar.controller.group;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.calendar.GroupCalendarService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class GroupController {

    private final GroupCalendarService groupCalendarService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/groups")
    public ResponseEntity<CalendarDto> createGroup(@RequestParam(value = "title", required = false) String title,
                                                   HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarDto result = groupCalendarService.create(userId, title);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/groups/{title}")
    public ResponseEntity<List<CalendarDto.Response>> findGroup(@NotNull @PathVariable("title") String title) {

        List<CalendarDto.Response> result = groupCalendarService.findGroup(title);

        if(result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(result);
    }

}
