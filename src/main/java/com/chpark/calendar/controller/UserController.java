package com.chpark.calendar.controller;

import com.chpark.calendar.dto.JwtAuthenticationResponseDto;
import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/login/users")
    public ResponseEntity<?> loginUser(@Validated @RequestBody UserDto userRequest, HttpServletResponse response) {
        try {
            String token = userService.loginUser(userRequest);

            // JWT 토큰을 HttpOnly 쿠키로 저장
            ResponseCookie cookie = ResponseCookie.from("jwtToken", token)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(new JwtAuthenticationResponseDto(token, "Login successful!"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + ex.getMessage() + "\"}");
        }
    }
}
