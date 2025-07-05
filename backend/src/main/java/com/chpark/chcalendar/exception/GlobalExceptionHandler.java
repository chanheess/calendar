package com.chpark.chcalendar.exception;

import com.chpark.chcalendar.dto.MessageResponseDto;
import com.chpark.chcalendar.exception.authorization.CalendarAuthorizationException;
import com.chpark.chcalendar.exception.authorization.GroupAuthorizationException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public MessageResponseDto createCustomErrorResponse(Exception ex, int status) {
        // 예외가 null이거나 메시지가 없는 경우 기본 메시지로 대체
        String errorMessage = (ex == null || ex.getMessage() == null || ex.getMessage().isEmpty())
                ? "Unknown error occurred."
                : ex.getMessage();

        return new MessageResponseDto(
                status,        // HTTP 상태 코드
                errorMessage   // 예외 메시지
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<MessageResponseDto> handleNotFoundEntityById(EntityNotFoundException ex) {
        return new ResponseEntity<>(
                createCustomErrorResponse(ex, HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(
                createCustomErrorResponse(ex, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<MessageResponseDto> handleAuthentication(AuthenticationException ex) {
        return new ResponseEntity<>(
                createCustomErrorResponse(ex, HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponseDto> handleAuthorizationException(AccessDeniedException ex) {
        return new ResponseEntity<>(
                createCustomErrorResponse(ex, HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(PasswordPolicyException.class)
    public ResponseEntity<MessageResponseDto> handlePasswordPolicyException(PasswordPolicyException ex) {
        return new ResponseEntity<>(
                createCustomErrorResponse(ex, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ScheduleException.class)
    public ResponseEntity<MessageResponseDto> handleScheduleException(ScheduleException ex) {
        return new ResponseEntity<>(
                createCustomErrorResponse(ex, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(CalendarAuthorizationException.class)
    public ResponseEntity<MessageResponseDto> handleCalendarAuthorizationException(CalendarAuthorizationException ex) {
        return new ResponseEntity<>(
                createCustomErrorResponse(ex, HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(GroupAuthorizationException.class)
    public ResponseEntity<MessageResponseDto> handleGroupAuthorizationException(GroupAuthorizationException ex) {
        return new ResponseEntity<>(
                createCustomErrorResponse(ex, HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<MessageResponseDto> handleExpiredJwtException(ExpiredJwtException ex) {
        return new ResponseEntity<>(
                createCustomErrorResponse(ex, HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

}
