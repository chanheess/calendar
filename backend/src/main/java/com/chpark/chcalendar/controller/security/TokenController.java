package com.chpark.chcalendar.controller.security;

import com.chpark.chcalendar.dto.security.JwtAuthenticationResponseDto;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.exception.authentication.TokenAuthenticationException;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.utility.CookieUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
@Slf4j
public class TokenController {
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/auth/refresh")
    public ResponseEntity<String> renewAccessToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            JwtAuthenticationResponseDto responseTokenDto = jwtTokenProvider.renewAccessToken(request);

            CookieUtility.setCookie(JwtTokenType.ACCESS, responseTokenDto.getAccessToken(), 60 * 60, response);
            CookieUtility.setCookie(JwtTokenType.REFRESH, responseTokenDto.getRefreshToken(), 14 * 24 * 60 * 60, response);

            return ResponseEntity.ok(responseTokenDto.getMessage());
        } catch (TokenAuthenticationException e) {
            return ResponseEntity.status(401).body("Token refresh failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error during token refresh");
        }
    }

}
