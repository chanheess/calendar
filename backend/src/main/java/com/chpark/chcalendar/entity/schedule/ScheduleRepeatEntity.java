package com.chpark.chcalendar.entity.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleRepeatDto;
import com.chpark.chcalendar.enumClass.ScheduleRepeatType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name="schedule_repeat")
public class ScheduleRepeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private ScheduleRepeatType repeatType;

    @Column(name = "repeat_interval", nullable = false)
    private int repeatInterval;

    //null == endless
    @Column(name = "end_at")
    private LocalDateTime endAt;

    public ScheduleRepeatEntity(ScheduleRepeatDto repeatDto) {
        this.repeatType = repeatDto.getRepeatType();
        this.repeatInterval = repeatDto.getRepeatInterval();
        this.endAt = repeatDto.getEndAt();
    }

}
