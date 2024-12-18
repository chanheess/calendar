package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleRepeatDto;
import com.chpark.calendar.exception.ValidGroup;
import com.chpark.calendar.security.JwtTokenProvider;
import com.chpark.calendar.service.ScheduleRepeatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Optional;

@RestController
@RequestMapping("/repeats")
@RequiredArgsConstructor
public class ScheduleRepeatController {

    private final ScheduleRepeatService scheduleRepeatService;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping
    public ResponseEntity<ScheduleRepeatDto> createScheduleRepeat(@RequestParam("scheduleId") long scheduleId,
                                                                  @Validated @RequestBody ScheduleRepeatDto repeatDto,
                                                                  HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
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
