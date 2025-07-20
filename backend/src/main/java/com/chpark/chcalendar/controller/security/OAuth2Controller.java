package com.chpark.chcalendar.controller.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    @PostMapping("/login")
    public String login(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("oauth2_type", "login");
        return "/oauth2/authorization/google";
    }

    @PostMapping("/link")
    public String link(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("oauth2_type", "link");
        return "/oauth2/authorization/google";
    }
}