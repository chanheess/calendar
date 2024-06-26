package com.chpark.calendar;

import com.chpark.calendar.repository.ScheduleRepository;
import com.chpark.calendar.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    private final ScheduleRepository scheduleRepository;

    @Autowired
    public SpringConfig(ScheduleRepository scheduleRepository) { this.scheduleRepository = scheduleRepository; }

    @Bean
    public ScheduleService scheduleService() { return new ScheduleService(scheduleRepository); }

}
