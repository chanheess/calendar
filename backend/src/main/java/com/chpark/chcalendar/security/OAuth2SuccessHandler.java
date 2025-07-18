package com.chpark.chcalendar.security;

import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.enumClass.OAuthLoginType;
import com.chpark.chcalendar.event.calendar.google.GoogleCalendarSyncEvent;
import com.chpark.chcalendar.event.schedule.google.GoogleScheduleSyncEvent;
import com.chpark.chcalendar.utility.CookieUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

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
        
        // 세션에서 OAuthLoginType 안전하게 가져오기
        Object sessionAttribute = request.getSession().getAttribute("oauth_login_type");
        OAuthLoginType type;
        
        if (sessionAttribute instanceof OAuthLoginType) {
            type = (OAuthLoginType) sessionAttribute;
        } else if (sessionAttribute instanceof String) {
            // String인 경우 enum으로 변환 시도
            try {
                type = OAuthLoginType.valueOf(((String) sessionAttribute).toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 값인 경우 기본값 사용
                type = OAuthLoginType.OAUTH;
            }
        } else {
            // null이거나 다른 타입인 경우 기본값 사용
            type = OAuthLoginType.OAUTH;
        }

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

        String accessToken = "";

        switch (type) {
            case LINK, LOCAL -> {
                request.getSession().removeAttribute("oauth_login_type");
                accessToken = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
            }
            case OAUTH -> {
                accessToken = jwtTokenProvider.generateToken(email, JwtTokenType.ACCESS);
                String refreshToken = jwtTokenProvider.generateToken(email, JwtTokenType.REFRESH);

                CookieUtility.setCookie(JwtTokenType.ACCESS, accessToken, 60 * 60, response);
                CookieUtility.setCookie(JwtTokenType.REFRESH, refreshToken, 14 * 24 * 60 * 60, response);
            }
        }

        long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

        eventPublisher.publishEvent(new GoogleCalendarSyncEvent(oauthAccessToken, userId));
        eventPublisher.publishEvent(new GoogleScheduleSyncEvent(oauthAccessToken, userId));

        response.sendRedirect(homeUrl);
    }
}