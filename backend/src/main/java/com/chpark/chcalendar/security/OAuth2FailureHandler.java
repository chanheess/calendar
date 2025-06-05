package com.chpark.chcalendar.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${home_url}")
    String homeUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String message = exception.getMessage();
        if (message == null) {
            message = "로그인에 실패했습니다.";
        }

        Cookie errorCookie = new Cookie("login_error", URLEncoder.encode(message, StandardCharsets.UTF_8));
        errorCookie.setPath("/");
        errorCookie.setMaxAge(5);
        response.addCookie(errorCookie);

        response.sendRedirect(homeUrl + "/auth/login");
    }
}
