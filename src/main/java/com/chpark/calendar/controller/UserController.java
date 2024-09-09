package com.chpark.calendar.controller;

import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register/users")
    public String createUser(@Validated @ModelAttribute UserDto.PostRequest userRequest,
                                             RedirectAttributes redirectAttributes, Model model) {
        try {
            userService.createUser(userRequest);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("userRequest", userRequest);
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다.");
        return "redirect:/login";
    }

}
