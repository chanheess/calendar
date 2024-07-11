package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/schedules")
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping
    public List<ScheduleDto> getAllSchedule() {
        return scheduleService.findAll();
    }

    @GetMapping("/date")
    public List<ScheduleDto> getSchedules(@RequestParam("year") Integer year, @RequestParam("month") Integer month, @RequestParam("day") Integer day) {

        log.info("Fetching schedules for year: {}, month: {}, day: {}", year, month, day);

        //empty
        if (year == null) {
            return Collections.emptyList();
        }

        // year
        if (month == null) {
            return scheduleService.getSchedulesForYear(year);
        }

        // month
        if (day == null) {
            return scheduleService.getSchedulesForMonth(year, month);
        }

        // day
        return scheduleService.getSchedulesForDate(year, month, day);
    }

    @GetMapping("/{title}")
    public List<ScheduleDto> findSchedulesByTitle(@PathVariable("title") String title) {
        return scheduleService.findSchedulesByTitle(title);
    }

    @PostMapping
    public ScheduleDto createSchedule(@RequestBody ScheduleDto schedule) {
        return scheduleService.create(schedule);
    }

    @PutMapping("/{id}")
    public ScheduleDto updateSchedule(@PathVariable("id") int id, @RequestBody ScheduleDto schedule) {
        return scheduleService.update(id, schedule).get();
    }

    @DeleteMapping("/{id}")
    public void deleteSchedule(@PathVariable("id") int id) {
        scheduleService.deleteById(id);
    }
}
