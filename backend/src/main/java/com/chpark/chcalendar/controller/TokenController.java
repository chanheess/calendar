package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.dto.security.JwtAuthenticationResponseDto;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/auth/refresh")
    public ResponseEntity<String> renewAccessToken(HttpServletRequest request, HttpServletResponse response) {
        JwtAuthenticationResponseDto responseTokenDto = jwtTokenProvider.renewAccessToken(request);

        ResponseCookie cookie = ResponseCookie.from(JwtTokenType.ACCESS.getValue(), responseTokenDto.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from(JwtTokenType.REFRESH.getValue(), responseTokenDto.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(responseTokenDto.getMessage());
    }

}
