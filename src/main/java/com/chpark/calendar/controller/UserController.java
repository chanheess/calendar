package com.chpark.calendar.controller;

import com.chpark.calendar.dto.JwtAuthenticationResponseDto;
import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.service.UserService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<?> loginUser(@Validated @RequestBody UserDto userRequest) {
        try {
            String token = userService.loginUser(userRequest);
            return ResponseEntity.ok(new JwtAuthenticationResponseDto(token, "Login successful!"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + ex.getMessage() + "\"}");
        }
    }
}
