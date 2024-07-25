package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleRepeatDto;
import com.chpark.calendar.service.ScheduleRepeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/repeat")
@RequiredArgsConstructor
public class ScheduleRepeatController {

    private final ScheduleRepeatService scheduleRepeatService;

    @PostMapping
    public ResponseEntity<ScheduleRepeatDto.Response> createScheduleRepeat(@RequestParam int scheduleId,
                                                                           @RequestBody ScheduleRepeatDto repeatDto) {

        ScheduleRepeatDto.Response createResponse = scheduleRepeatService.create(scheduleId, repeatDto);

        return new ResponseEntity<>(createResponse, HttpStatus.CREATED);
    }
}
