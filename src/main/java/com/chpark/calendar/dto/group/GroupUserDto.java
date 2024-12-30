package com.chpark.calendar.dto.group;

import com.chpark.calendar.enumClass.GroupAuthority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupUserDto {

    @NotNull
    private String groupTitle;
    @NotNull
    private Long groupId;
    @NotNull
    private Long userId;
    @NotNull
    private GroupAuthority role;
}