package com.chpark.calendar.controller;

import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.service.UserService;
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

    @GetMapping("/auth/login")
    public String login() {
        return "login";
    }

    @GetMapping("/auth/register")
    public String register(Model model) {
        model.addAttribute("userRequest", new UserDto.RegisterRequest());
        return "register";
    }

    @PostMapping("/auth/register")
    public String createUser(@Validated @ModelAttribute UserDto.RegisterRequest userRequest,
                             RedirectAttributes redirectAttributes, Model model) {
        try {
            userService.createUser(userRequest);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("userRequest", userRequest);
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다.");
        return "redirect:/auth/login";
    }
}

