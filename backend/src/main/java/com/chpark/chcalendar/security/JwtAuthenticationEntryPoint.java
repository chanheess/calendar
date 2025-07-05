package com.chpark.chcalendar.security;

import com.chpark.chcalendar.dto.MessageResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        MessageResponseDto jwtErrorResponse = new MessageResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                authException.getCause() != null ? authException.getCause().getMessage() : authException.getMessage()
        );
        String result = objectMapper.writeValueAsString(jwtErrorResponse);
        response.getWriter().write(result);
    }
}