package com.chpark.chcalendar.entity.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleGroupDto;
import com.chpark.chcalendar.enumClass.FileAuthority;
import com.chpark.chcalendar.enumClass.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name="schedule_group")
public class ScheduleGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileAuthority authority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(name = "schedule_id")
    private long scheduleId;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "user_nickname")
    private String userNickname;

    public ScheduleGroupEntity(long scheduleId, ScheduleGroupDto scheduleGroupDto) {
        this.authority = scheduleGroupDto.getAuthority();
        this.status = scheduleGroupDto.getStatus();
        this.userId = scheduleGroupDto.getUserId();
        this.userNickname = scheduleGroupDto.getUserNickname();
        this.scheduleId = scheduleId;
    }

    public static List<ScheduleGroupEntity> fromScheduleGroupDtoList(long scheduleId, List<ScheduleGroupDto> dtoList) {
        return dtoList.stream()
                .map(dto -> new ScheduleGroupEntity(scheduleId, dto))
                .collect(Collectors.toList());
    }
}
