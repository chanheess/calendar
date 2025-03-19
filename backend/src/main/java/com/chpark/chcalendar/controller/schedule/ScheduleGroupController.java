package com.chpark.chcalendar.controller.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleGroupDto;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.schedule.ScheduleGroupService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api")
@Controller
public class ScheduleGroupController {

    private final ScheduleGroupService scheduleGroupService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/schedules/{scheduleId}/group")
    public ResponseEntity<List<ScheduleGroupDto>> getScheduleGroup(@PathVariable("scheduleId") long scheduleId,
                                                                   HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        return ResponseEntity.ok(scheduleGroupService.getScheduleGroupUserList(userId, scheduleId));
    }

    @PutMapping("/schedules/{scheduleId}/group")
    public ResponseEntity<ScheduleGroupDto> updateScheduleGroup(@PathVariable("scheduleId") long scheduleId,
                                                                @RequestBody ScheduleGroupDto requestGroup) {

        return ResponseEntity.ok(scheduleGroupService.updateScheduleGroup(scheduleId, requestGroup));
    }

}
