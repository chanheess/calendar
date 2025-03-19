package com.chpark.chcalendar.entity.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleGroupDto;
import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.chpark.chcalendar.enumClass.FileAuthority;
import com.chpark.chcalendar.enumClass.InvitationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="schedule_group")
public class ScheduleGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileAuthority authority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "schedule_id")
    private long scheduleId;

    public ScheduleGroupEntity(long scheduleId, ScheduleGroupDto scheduleGroupDto) {
        this.authority = scheduleGroupDto.getAuthority();
        this.status = scheduleGroupDto.getStatus();
        this.userId = scheduleGroupDto.getUserId();
        this.scheduleId = scheduleId;
    }

    public static List<ScheduleGroupEntity> fromScheduleGroupDtoList(long scheduleId, List<ScheduleGroupDto> dtoList) {
        return dtoList.stream()
                .map(dto -> new ScheduleGroupEntity(scheduleId, dto))
                .collect(Collectors.toList());
    }

    public static List<ScheduleGroupEntity> fromScheduleGroupDtoListWithId(long scheduleId, List<ScheduleGroupDto> dtoList) {
        return dtoList.stream()
                .map(dto -> new ScheduleGroupEntity(dto.getId(), dto.getAuthority(), dto.getStatus(), dto.getUserId(), scheduleId))
                .collect(Collectors.toList());
    }
}
