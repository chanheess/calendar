package com.chpark.chcalendar.security;

import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.utility.CookieUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${home_url}")
    String homeUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                token.getAuthorizedClientRegistrationId(),
                token.getName()
        );

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String type = (String) request.getSession().getAttribute("oauth2_type");

        // OAuth API용 토큰 저장
        String oauthAccessToken = client.getAccessToken().getTokenValue();
        String oauthRefreshToken = client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null;

        CookieUtility.setCookie(JwtTokenType.GOOGLE_ACCESS,
                oauthAccessToken,
                Duration.between(Instant.now(), client.getAccessToken().getExpiresAt()).getSeconds(),
                response
        );

        if (oauthRefreshToken != null) {
            CookieUtility.setCookie(JwtTokenType.GOOGLE_REFRESH,
                    oauthRefreshToken,
                    Duration.between(Instant.now(), client.getRefreshToken().getExpiresAt()).getSeconds(),
                    response
            );
        }

        if ("link".equals(type)) {
            request.getSession().removeAttribute("oauth2_type");
            return;
        }

        // 사용자로부터 local login JWT 발급
        String accessToken = jwtTokenProvider.generateToken(email, JwtTokenType.ACCESS);
        String refreshToken = jwtTokenProvider.generateToken(email, JwtTokenType.REFRESH);

        CookieUtility.setCookie(JwtTokenType.ACCESS, accessToken, 60 * 60, response);
        CookieUtility.setCookie(JwtTokenType.REFRESH, refreshToken, 14 * 24 * 60 * 60, response);

        // 리다이렉트
        response.sendRedirect(homeUrl); // 로그인 후 이동할 페이지
    }
}