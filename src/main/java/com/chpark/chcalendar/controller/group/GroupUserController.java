package com.chpark.chcalendar.controller.group;

import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.dto.group.GroupUserDto;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.group.GroupUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class GroupUserController {

    private final GroupUserService groupUserService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/groups")
    public ResponseEntity<List<CalendarInfoDto>> findMyGroups(HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<CalendarInfoDto> result = groupUserService.findMyGroup(userId);

        if(result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/groups/{group-id}/users")
    public ResponseEntity<GroupUserDto> addUser(@NotNull @PathVariable("group-id") Long groupId,
                                                HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        GroupUserDto result = groupUserService.addUser(userId, groupId);

        return ResponseEntity.ok(result);
    }
}
