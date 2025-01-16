package com.chpark.chcalendar.controller.group;

import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.calendar.GroupCalendarService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class GroupController {

    private final GroupCalendarService groupCalendarService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/groups")
    public ResponseEntity<CalendarInfoDto> createGroup(@RequestParam(value = "title", required = false) String title,
                                                                HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CalendarInfoDto result = groupCalendarService.create(userId, title);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/groups/{title}")
    public ResponseEntity<List<CalendarInfoDto.Response>> findGroup(@NotNull @PathVariable("title") String title) {

        List<CalendarInfoDto.Response> result = groupCalendarService.findGroup(title);

        if(result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(result);
    }

}
