package com.chpark.chcalendar.controller.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    @PostMapping("/login")
    public String login(@RequestBody String type,
                        HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("oauth_login_type", type);
        return "/oauth2/authorization/google";
    }
}