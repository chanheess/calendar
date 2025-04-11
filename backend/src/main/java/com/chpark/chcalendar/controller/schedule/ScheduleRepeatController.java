package com.chpark.chcalendar.controller.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleRepeatDto;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.schedule.ScheduleRepeatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/repeats")
@RequiredArgsConstructor
public class ScheduleRepeatController {

    private final ScheduleRepeatService scheduleRepeatService;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping
    public ResponseEntity<ScheduleRepeatDto> createScheduleRepeat(@RequestParam("scheduleId") long scheduleId,
                                                                  @Validated @RequestBody ScheduleRepeatDto repeatDto,
                                                                  HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        ScheduleRepeatDto createResponse = scheduleRepeatService.create(scheduleId, repeatDto, userId);

        return new ResponseEntity<>(createResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleRepeatDto> getScheduleRepeat(@PathVariable("id") long id) {
        Optional<ScheduleRepeatDto> findResponse = scheduleRepeatService.findById(id);

        return findResponse.map(repeatDto -> new ResponseEntity<>(repeatDto, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.OK));
    }
}
