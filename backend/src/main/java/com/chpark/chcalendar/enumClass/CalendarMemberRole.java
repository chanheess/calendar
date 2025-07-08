package com.chpark.chcalendar.enumClass;

public enum CalendarMemberRole {
    ADMIN,
    SUB_ADMIN,
    USER,
    READ;

    public static CalendarMemberRole parseCalendarMemberRole(String auth) {
        if (auth == null || auth.isEmpty()) return READ;

        auth = auth.toLowerCase();

        if (auth.contains("owner") || auth.contains("admin")) {
            return ADMIN;
        }

        if (auth.contains("sub_admin")) {
            return SUB_ADMIN;
        }

        if (auth.contains("user")) {
            return USER;
        }

        return READ;
    }
}

//admin: 모든 일정 crud 권한, 캘린더 모든 권한
//sub_admin: 모든 일정 crud 권한, 캘린더 유저 초대 권한, 캘린더 유저 권한 수정, 캘린더 색상 변경
//user: 자신이 만든 일정 crud 권한, 나머지 일정 수정, 읽기 권한(삭제X), 캘린더 색상 변경
//read: 일정 읽기 권한, 캘린더 색상 변경
