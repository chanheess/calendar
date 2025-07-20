package com.chpark.chcalendar.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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
        String message;
        
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauthException = (OAuth2AuthenticationException) exception;
            String errorCode = oauthException.getError().getErrorCode();
            
            switch (errorCode) {
                case "invalid_provider":
                    message = "해당 이메일은 다른 로그인 방식으로 이미 가입되어 있습니다.";
                    break;
                case "invalid_request":
                    message = "연동할 계정이 존재하지 않습니다.";
                    break;
                default:
                    message = "OAuth 인증에 실패했습니다: " + oauthException.getError().getDescription();
            }
        } else {
            message = exception.getMessage() != null ? exception.getMessage() : "OAuth 인증에 실패했습니다.";
        }

        // 에러 메시지를 쿠키로 전달
        Cookie errorCookie = new Cookie("oauth_error", URLEncoder.encode(message, StandardCharsets.UTF_8));
        errorCookie.setPath("/");
        errorCookie.setMaxAge(5);
        response.addCookie(errorCookie);

        // 현재 페이지로 리다이렉트 (Referer 헤더 사용)
        String referer = request.getHeader("Referer");
        String redirectUrl;
        
        if (referer != null && (referer.contains("/auth/login") || referer.contains("/user/profile"))) {
            // 로그인 페이지나 프로필 페이지에서 온 경우 해당 페이지로 리다이렉트
            redirectUrl = referer;
        } else {
            // 기본적으로 로그인 페이지로 리다이렉트
            redirectUrl = homeUrl + "/auth/login";
        }
        
        response.sendRedirect(redirectUrl);
    }
}
