package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleRepeatDto;
import com.chpark.calendar.service.ScheduleRepeatService;
import jakarta.persistence.EntityNotFoundException;
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

    @PatchMapping("/{id}")
    public ResponseEntity<ScheduleRepeatDto.Response> updateScheduleRepeat(@PathVariable("id") int id,
                                                                           @Valid @RequestBody ScheduleRepeatDto repeatDto) {
        if(!scheduleRepeatService.existsById(id)) {
            throw new EntityNotFoundException();
        }
        //현재 일정만 바꿀 것인가 나머지 일정 모두 바꿀 것인가?

        ScheduleRepeatDto.Response updateResponse = scheduleRepeatService.currentScheduleUpdate(id, repeatDto);

        return new ResponseEntity<>(updateResponse, HttpStatus.OK);
    }
}
