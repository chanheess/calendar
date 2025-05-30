package com.chpark.chcalendar.security;

import com.chpark.chcalendar.enumClass.JwtTokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2JwtTokenProvider oAuth2JwtTokenProvider;

    @Value("${home_url}")
    String homeUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        // 사용자로부터 JWT 발급
        String accessToken = oAuth2JwtTokenProvider.generateToken(email, JwtTokenType.ACCESS);
        String refreshToken = oAuth2JwtTokenProvider.generateToken(email, JwtTokenType.REFRESH);

        ResponseCookie cookie = ResponseCookie.from(JwtTokenType.ACCESS.getValue(), accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from(JwtTokenType.REFRESH.getValue(), refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // 리다이렉트
        response.sendRedirect(homeUrl); // 로그인 후 이동할 페이지
    }
}