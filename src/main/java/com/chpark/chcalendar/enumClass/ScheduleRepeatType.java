package com.chpark.chcalendar.enumClass;

import lombok.Getter;

//TODO: 나중에 value가 d,w...이렇게하고 실제 enum은 대문자로 바꿀 것
@Getter
public enum ScheduleRepeatType {
    d,  //day
    w,  //week
    m,  //month
    y;  //year
}

