package com.chpark.calendar.controller;

import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.service.CalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedules")
public class CalendarController {

    private static final Logger logger = LoggerFactory.getLogger(CalendarController.class);
    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/all")
    public List<ScheduleEntity> getAllSchedules() {
        return calendarService.findAll();
    }

    @GetMapping("/{title}")
    public List<ScheduleEntity> getScheduleById(@PathVariable String title) {
        return calendarService.findSchedulesByTitle(title);
    }

    @PostMapping
    public ScheduleEntity createSchedule(@RequestBody ScheduleEntity schedule) {
        return calendarService.create(schedule);
    }

    @PutMapping("/{id}")
    public ScheduleEntity updateSchedule(@PathVariable int id, @RequestBody ScheduleEntity schedule) {
        schedule.setId(id);
        return calendarService.update(schedule);
    }

    @DeleteMapping("/{id}")
    public void deleteSchedule(@PathVariable int id) {
        calendarService.delete(id);
    }

    @GetMapping("/month")
    public List<ScheduleEntity> getSchedulesForMonth(@RequestParam("year") int year, @RequestParam("month") int month) {
        return calendarService.getSchedulesForMonth(year, month);
    }

    @GetMapping("/date")
    public List<ScheduleEntity> getSchedulesForDate(@RequestParam("year") int year, @RequestParam("month") int month, @RequestParam("day") int day) {
        logger.info("Fetching schedules for year: {}, month: {}, day: {}", year, month, day);
        return calendarService.getSchedulesForDate(year, month, day);
    }


}
