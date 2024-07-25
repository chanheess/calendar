package com.chpark.calendar.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Entity
public class ScheduleRepeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int scheduleId;

    //'D' (매일), 'W' (매주), 'M' (매달), 'Y' (매년)
    @Pattern(regexp = "[DWMY]", message = "Repeat type must be one of 'D', 'W', 'M', 'Y'")
    @Column(name = "repeat_type", nullable = false, length = 1)
    private String repeatType;

    @Column(name = "repeat_interval", nullable = false)
    private int repeatInterval;

    //null == endless
    @Column(name = "end_at")
    private LocalDateTime endAt;

}
