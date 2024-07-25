package com.chpark.calendar.entity;

import com.chpark.calendar.dto.ScheduleRepeatDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Entity
@Getter
@Table(name="schedule_repeat")
public class ScheduleRepeatEntity {

    @Id
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

    public ScheduleRepeatEntity(int scheduleId, ScheduleRepeatDto repeatDto) {
        this.scheduleId = scheduleId;
        this.repeatType = repeatDto.getRepeatType();
        this.repeatInterval = repeatDto.getRepeatInterval();
        this.endAt = repeatDto.getEndAt();
    }

}
