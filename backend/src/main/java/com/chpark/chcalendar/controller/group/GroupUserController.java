package com.chpark.chcalendar.controller.group;

import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.dto.group.GroupUserDto;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.user.GroupUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class GroupUserController {

    private final GroupUserService groupUserService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/groups")
    public ResponseEntity<List<CalendarInfoDto.Response>> findMyGroups(HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<CalendarInfoDto.Response> result = groupUserService.findMyGroup(userId);

        if(result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/groups/{group-id}/users")
    public ResponseEntity<List<GroupUserDto>> getUsers(@NotNull @PathVariable("group-id") Long groupId,
                                                           HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<GroupUserDto> result = groupUserService.findGroupUserList(userId, groupId);

        return ResponseEntity.ok(result);
    }
}
