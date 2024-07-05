package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping
    public List<ScheduleDto> getSchedules(@RequestParam("year") Optional<Integer> year,
                                          @RequestParam("month") Optional<Integer> month,
                                          @RequestParam("day") Optional<Integer> day,
                                          @RequestParam("title") Optional<String> title) {

        log.info("Fetching schedules for year: {}, month: {}, day: {}, title: {}", year, month, day, title);

        // Title
        if (title.isPresent()) {
            return scheduleService.findSchedulesByTitle(title.get());
        }

        // All
        if (year.isEmpty() && month.isEmpty() && day.isEmpty()) {
            return scheduleService.findAll();
        }

        // year
        if (year.isPresent() && month.isEmpty() && day.isEmpty()) {
            return scheduleService.getSchedulesForYear(year.get());
        }

        // month
        if (year.isPresent() && month.isPresent() && day.isEmpty()) {
            return scheduleService.getSchedulesForMonth(year.get(), month.get());
        }

        // day
        if (year.isPresent() && month.isPresent() && day.isPresent()) {
            return scheduleService.getSchedulesForDate(year.get(), month.get(), day.get());
        }

        return Collections.emptyList();
    }

    @PostMapping
    public ScheduleDto createSchedule(@RequestBody ScheduleDto schedule) {
        return scheduleService.create(schedule);
    }

    @PutMapping()
    public ScheduleDto updateSchedule(@RequestBody ScheduleDto schedule) {
        return scheduleService.update(schedule).get();
    }

    @DeleteMapping("/{id}")
    public void deleteSchedule(@PathVariable int id) {
        scheduleService.delete(id);
    }
}
