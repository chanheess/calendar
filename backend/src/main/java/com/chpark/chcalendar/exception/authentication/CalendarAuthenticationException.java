package com.chpark.chcalendar.exception.authentication;

import org.springframework.security.core.AuthenticationException;

public class CalendarAuthenticationException extends AuthenticationException {
    public CalendarAuthenticationException(String message) {
        super(message);
    }
}
