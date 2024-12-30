package com.chpark.calendar.controller.group;

import com.chpark.calendar.dto.group.GroupDto;
import com.chpark.calendar.security.JwtTokenProvider;
import com.chpark.calendar.service.group.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class GroupController {

    private final GroupService groupService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/groups")
    public ResponseEntity<GroupDto> createGroup(@RequestParam(value = "title", required = false) String title,
                                               HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        GroupDto result = groupService.create(userId, title);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/groups/{title}")
    public ResponseEntity<List<GroupDto>> findGroup(@NotNull @PathVariable("title") String title) {

        List<GroupDto> result = groupService.findGroup(title);

        if(result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(result);
    }

}
