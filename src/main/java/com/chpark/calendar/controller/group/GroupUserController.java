package com.chpark.calendar.controller.group;

import com.chpark.calendar.dto.group.GroupDto;
import com.chpark.calendar.dto.group.GroupUserDto;
import com.chpark.calendar.security.JwtTokenProvider;
import com.chpark.calendar.service.group.GroupUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class GroupUserController {

    private final GroupUserService groupUserService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/groups")
    public ResponseEntity<List<GroupDto>> findMyGroups(HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<GroupDto> result = groupUserService.findMyGroup(userId);

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
