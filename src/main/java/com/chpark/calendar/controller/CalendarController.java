package com.chpark.calendar.controller;

import com.chpark.calendar.service.CalendarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }

//    @PostMapping("/Schedule/new")


}
