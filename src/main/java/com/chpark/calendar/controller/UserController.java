package com.chpark.calendar.controller;

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
    public ResponseEntity<String> loginUser(@Validated @RequestBody UserDto userRequest) {
        try {
            String token = userService.loginUser(userRequest);
            return ResponseEntity.ok().header("Authorization", "Bearer " + token).body("{\"message\": \"Login successful\"}");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + ex.getMessage() + "\"}");
        }
    }
}
