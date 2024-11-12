package com.chpark.calendar.controller;

import com.chpark.calendar.dto.JwtAuthenticationResponseDto;
import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.security.JwtTokenProvider;
import com.chpark.calendar.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@Validated @RequestBody UserDto userRequest, HttpServletResponse response) {
        try {
            String token = userService.login(userRequest);

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

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        try {
            // jwtToken 쿠키 삭제
            ResponseCookie cookie = ResponseCookie.from("jwtToken", null)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)  // 만료 시간 0으로 설정하여 쿠키 삭제
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok("Logged out successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + ex.getMessage() + "\"}");
        }
    }

    @GetMapping("/auth/check")
    public ResponseEntity<Boolean> checkLogin(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);

        if(token != null && jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.ok().body(true);
        } else {
            return ResponseEntity.ok().body(false);
        }
    }

    @GetMapping("/user/nickname")
    public ResponseEntity<String> getUserNickname(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        int userId = jwtTokenProvider.getUserIdFromToken(token);

        try {
            String nickname = userService.findNickname(userId);
            return ResponseEntity.ok().body(nickname);
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @GetMapping("/user/info")
    public ResponseEntity<UserDto.UserInfo> getUserInfo(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        int userId = jwtTokenProvider.getUserIdFromToken(token);

        return ResponseEntity.ok().body(userService.findUserInfo(userId));
    }

    @PatchMapping("/user/info")
    public ResponseEntity<String> updateUserInfo(@RequestBody UserDto.UserInfo userInfo, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        int userId = jwtTokenProvider.getUserIdFromToken(token);

        try {
            userService.updateUserInfo(userId, userInfo);
            return ResponseEntity.ok().body("Edit successfully");

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @PatchMapping("/user/password")
    public ResponseEntity<String> updatePassword(@Validated @RequestBody UserDto.ChangePassword changePassword, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        int userId = jwtTokenProvider.getUserIdFromToken(token);

        try {
            userService.updatePassword(userId, changePassword);
            return ResponseEntity.ok().body("Password updated successfully.");
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }
}
