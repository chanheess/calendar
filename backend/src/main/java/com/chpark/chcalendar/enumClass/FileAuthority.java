package com.chpark.chcalendar.enumClass;

public enum FileAuthority {
    ADMIN,
    WRITE,
    READ;

    public static FileAuthority parseFileAuthority(String auth) {
        if (auth == null || auth.isEmpty()) return ADMIN;

        auth = auth.toLowerCase();

        if (auth.contains("owner") || auth.contains("admin")) {
            return ADMIN;
        }

        if (auth.contains("write")) {
            return WRITE;
        }

        return READ;
    }
}
