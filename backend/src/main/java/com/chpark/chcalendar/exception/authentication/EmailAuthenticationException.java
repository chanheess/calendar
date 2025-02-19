package com.chpark.chcalendar.exception.authentication;

import org.springframework.security.core.AuthenticationException;

public class EmailAuthenticationException extends AuthenticationException {
    public EmailAuthenticationException(String message) {
        super(message);
    }
}