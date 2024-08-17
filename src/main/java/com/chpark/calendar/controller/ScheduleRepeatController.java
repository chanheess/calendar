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

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleRepeatDto.Response> getScheduleRepeat(@PathVariable("id") int id) {
        ScheduleRepeatDto.Response findResponse = scheduleRepeatService.findById(id);

        return new ResponseEntity<>(findResponse, HttpStatus.OK);
    }
}
