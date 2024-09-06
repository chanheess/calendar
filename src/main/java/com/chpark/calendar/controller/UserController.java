package com.chpark.calendar.controller;

import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<String> createUser(@Validated @RequestBody UserDto.PostRequest userRequest) {
        userService.createUser(userRequest);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
