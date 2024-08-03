package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleRepeatDto;
import com.chpark.calendar.service.ScheduleRepeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/repeats")
@RequiredArgsConstructor
public class ScheduleRepeatController {

    private final ScheduleRepeatService scheduleRepeatService;

    @PostMapping
    public ResponseEntity<ScheduleRepeatDto.Response> createScheduleRepeat(@RequestParam("scheduleId") int scheduleId,
                                                                           @Valid @RequestBody ScheduleRepeatDto repeatDto) throws SQLException {

        ScheduleRepeatDto.Response createResponse = scheduleRepeatService.create(scheduleId, repeatDto);

        return new ResponseEntity<>(createResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleRepeatDto.Response> getScheduleRepeat(@PathVariable("scheduleId") int scheduleId) {

        ScheduleRepeatDto.Response findResponse = scheduleRepeatService.findById(scheduleId);

        return new ResponseEntity<>(findResponse, HttpStatus.OK);
    }

    @PatchMapping("/{scheduleId}")
    public ResponseEntity<ScheduleRepeatDto.Response> updateScheduleRepeat(@PathVariable("scheduleId") int scheduleId,
                                                                           @Valid @RequestBody ScheduleRepeatDto repeatDto) {

        ScheduleRepeatDto.Response updateResponse = scheduleRepeatService.update(scheduleId, repeatDto);

        return new ResponseEntity<>(updateResponse, HttpStatus.OK);
    }
}
