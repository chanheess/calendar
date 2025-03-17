package com.chpark.chcalendar.dto.schedule;

import com.chpark.chcalendar.enumClass.FileAuthority;
import com.chpark.chcalendar.enumClass.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleGroupDto {

    private FileAuthority authority;
    private InvitationStatus status;

    private long userId;
    private long scheduleId;
}
