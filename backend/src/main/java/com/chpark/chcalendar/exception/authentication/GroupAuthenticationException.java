package com.chpark.chcalendar.exception.authentication;

import org.springframework.security.core.AuthenticationException;

public class GroupAuthenticationException extends AuthenticationException {
    public GroupAuthenticationException(String message) {
        super(message);
    }
}