package com.chpark.chcalendar.dto.calendar;

import com.chpark.chcalendar.enumClass.CalendarMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class CalendarMemberDto {
    @NotNull
    private Long calendarId;
    @NotNull
    private Long userId;
    @NotNull
    private CalendarMemberRole role;
    @NotNull
    private String userNickname;

}