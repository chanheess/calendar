package com.chpark.chcalendar.exception.authentication;

import org.springframework.security.core.AuthenticationException;

public class PasswordAuthenticationException extends AuthenticationException {
    public PasswordAuthenticationException(String message) {
        super(message);
    }
}