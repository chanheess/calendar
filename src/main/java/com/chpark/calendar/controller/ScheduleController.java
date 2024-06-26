package com.chpark.calendar.controller;

import com.chpark.calendar.domain.Schedule;
import com.chpark.calendar.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/schedules/new")
    public String createForm() {
        return "schedules/createScheduleForm";
    }

    @PostMapping("/schedules/new")
    public String createSchedule(ScheduleForm scheduleForm) {
        Schedule schedule = new Schedule();
        schedule.setTitle(scheduleForm.getTitle());
        schedule.setStartDate(scheduleForm.getStartDate());
        schedule.setEndDate(scheduleForm.getEndDate());
        schedule.setDescription(scheduleForm.getDescription());

        scheduleService.createSchedule(schedule);

        return "redirect:/";
    }
}
