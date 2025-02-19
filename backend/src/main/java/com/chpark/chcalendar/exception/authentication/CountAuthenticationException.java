package com.chpark.chcalendar.exception.authentication;

import org.springframework.security.core.AuthenticationException;

public class CountAuthenticationException extends AuthenticationException {
    public CountAuthenticationException(String message) {
        super(message);
    }
}
