package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.service.RedisService;
import com.chpark.chcalendar.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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