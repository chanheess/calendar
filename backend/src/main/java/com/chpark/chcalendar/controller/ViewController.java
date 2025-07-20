package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.dto.user.UserDto;
import com.chpark.chcalendar.service.redis.RedisService;
import com.chpark.chcalendar.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ViewController {
    private final UserService userService;
    private final RedisService redisService;

    @GetMapping("/auth/login")
    public String login() {
        return "login";
    }

    @GetMapping("/auth/register")
    public String register(Model model) {
        model.addAttribute("userRequest", new UserDto.RegisterRequest());
        return "register";
    }

    @GetMapping("/user/profile")
    public String userProfile() {
        return "userProfile";
    }
}