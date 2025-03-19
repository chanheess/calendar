package com.chpark.chcalendar.dto.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleGroupEntity;
import com.chpark.chcalendar.enumClass.FileAuthority;
import com.chpark.chcalendar.enumClass.InvitationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class ScheduleGroupDto {

    private Long id;

    private FileAuthority authority;

    private InvitationStatus status = InvitationStatus.DECLINED;

    private long userId;

    public ScheduleGroupDto(ScheduleGroupEntity scheduleGroupEntity) {
        this.id = scheduleGroupEntity.getId();
        this.authority = scheduleGroupEntity.getAuthority();
        this.status = scheduleGroupEntity.getStatus();
        this.userId = scheduleGroupEntity.getUserId();
    }

    public ScheduleGroupDto(long id, InvitationStatus status, long userId) {
        this.id = id;
        this.status = status;
        this.userId = userId;
    }

    public ScheduleGroupDto(FileAuthority authority, InvitationStatus status, long userId) {

        this.authority = authority;
        this.status = status;
        this.userId = userId;
    }

    public static List<ScheduleGroupDto> fromScheduleGroupEntityList(List<ScheduleGroupEntity> groupList) {
        return groupList.stream()
                .map(ScheduleGroupDto::new)
                .collect(Collectors.toList());
    }

    public static List<ScheduleGroupDto> fromUnauthorizedUserEntityList(List<ScheduleGroupEntity> groupList) {
        return groupList.stream()
                .map(entity -> new ScheduleGroupDto(entity.getId(), entity.getStatus(), entity.getUserId()))
                .collect(Collectors.toList());
    }
}
