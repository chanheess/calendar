package com.chpark.chcalendar.exception;

import com.chpark.chcalendar.dto.MessageResponseDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<MessageResponseDto> handleIllegalArgument(AuthenticationException ex) {
        return new ResponseEntity<>(
                createCustomErrorResponse(ex, HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(GroupAuthorityException.class)
    public ResponseEntity<MessageResponseDto> handleIllegalArgument(GroupAuthorityException ex) {
        return new ResponseEntity<>(
                createCustomErrorResponse(ex, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }
}
