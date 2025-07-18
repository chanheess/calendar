package com.chpark.chcalendar.dto.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleGroupEntity;
import com.chpark.chcalendar.enumClass.FileAuthority;
import com.chpark.chcalendar.enumClass.InvitationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleGroupDto {

    private Long id;

    private FileAuthority authority;

    private InvitationStatus status = InvitationStatus.DECLINED;

    private long userId;

    private String userNickname;

    public ScheduleGroupDto(ScheduleGroupEntity scheduleGroupEntity) {
        this.id = scheduleGroupEntity.getId();
        this.authority = scheduleGroupEntity.getAuthority();
        this.status = scheduleGroupEntity.getStatus();
        this.userId = scheduleGroupEntity.getUserId();
        this.userNickname = scheduleGroupEntity.getUserNickname();
    }

    public static List<ScheduleGroupDto> fromScheduleGroupEntityList(List<ScheduleGroupEntity> groupList) {
        return groupList.stream()
                .map(ScheduleGroupDto::new)
                .collect(Collectors.toList());
    }
}
