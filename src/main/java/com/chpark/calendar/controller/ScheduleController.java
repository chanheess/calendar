package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.exception.CustomException;
import com.chpark.calendar.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/schedules")
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleDto>> getSchedulesByTitle(@RequestParam(value = "title", required = false) String title) {

        List<ScheduleDto> schedules;

        if(title.isEmpty()) {
            schedules = scheduleService.findAll();
        } else {
            schedules = scheduleService.findSchedulesByTitle(title);
        }

        if(schedules.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        }
    }

    @GetMapping("/date")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByDate(@RequestParam("year") Integer year,
                                                                @RequestParam(value = "month", required = false) Integer month,
                                                                @RequestParam(value = "day", required = false) Integer day) {

        log.info("Fetching schedules for year: {}, month: {}, day: {}", year, month, day);

        List<ScheduleDto> schedules;

        if (month != null && day != null) {
            //day의 범위를 잡기 위함
            LocalDate.of(year, month, day);

            schedules =  scheduleService.getSchedulesForDate(year, month, day);
        } else if (month != null) {
            if (month < 1 || month > 12) {
                throw new CustomException("Month parameter must be between 1 and 12.");
            }

            schedules =  scheduleService.getSchedulesForMonth(year, month);
        } else {
            schedules =  scheduleService.getSchedulesForYear(year);
        }

        if(schedules.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        }

    }

    @PostMapping
    public ResponseEntity<ScheduleDto> createSchedule(@RequestBody ScheduleDto schedule) {
        Optional<ScheduleDto> createDto = scheduleService.create(schedule);

        if(createDto.isPresent()) {
            return new ResponseEntity<>(createDto.get(), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleDto> updateSchedule(@PathVariable("id") int id, @RequestBody ScheduleDto schedule) {
        Optional<ScheduleDto> updateDto = scheduleService.update(id, schedule);

        if(updateDto.isPresent()) {
            return new ResponseEntity<>(updateDto.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSchedule(@PathVariable("id") int id) {
        if (!scheduleService.existsById(id)) {
            return new ResponseEntity<>("Schedule not found.", HttpStatus.NOT_FOUND);
        }

        scheduleService.deleteById(id);
        return new ResponseEntity<>("Schedule deleted successfully.", HttpStatus.OK);
    }
}
