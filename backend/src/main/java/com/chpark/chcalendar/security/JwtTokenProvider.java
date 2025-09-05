package com.chpark.chcalendar.security;

import com.chpark.chcalendar.dto.security.JwtAuthenticationResponseDto;
import com.chpark.chcalendar.entity.UserProviderEntity;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.exception.authentication.TokenAuthenticationException;
import com.chpark.chcalendar.repository.user.UserProviderRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${JWT_SECRET}")
    private String SECRET_KEY;

    @Getter
    private Key key;

    @Getter
    private long EXPIRATION_TIME = 60 * 60 * 1000;

    @Getter
    private long REFRESH_EXPIRATION_TIME = 14 * 24 * 60 * 60 * 1000;

    @Getter
    private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    private final UserProviderRepository userProviderRepository;

    // JWT 토큰 생성
    public String generateToken(Authentication authentication, long userId, JwtTokenType tokenType) {
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        long expirationTime = (tokenType == JwtTokenType.ACCESS) ? EXPIRATION_TIME : REFRESH_EXPIRATION_TIME;

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId) // 사용자 ID를 payload에 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    @Transactional
    public String generateToken(String email, JwtTokenType tokenType) {
        List<UserProviderEntity> userProviderEntity = userProviderRepository.findByProviderEmail(email);

        if (userProviderEntity.get(0) == null) {
            throw new OAuth2AuthenticationException("존재하지 않는 사용자입니다.");
        }

        if (userProviderEntity.get(0).getUser() == null) {
            throw new OAuth2AuthenticationException("존재하지 않는 사용자입니다.");
        }

        long expiration = switch (tokenType) {
            case ACCESS -> getEXPIRATION_TIME();
            case REFRESH -> getREFRESH_EXPIRATION_TIME();
            case GOOGLE_ACCESS, GOOGLE_REFRESH -> 0L;
        };

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userProviderEntity.get(0).getUser().getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), getSignatureAlgorithm())
                .compact();
    }

    // 쿠키에서 JWT 토큰 추출
    public String resolveToken(HttpServletRequest request, String tokenName) {
        // Authorization 헤더 먼저 확인
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후 토큰
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (tokenName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // JWT 토큰 검증
    public boolean validateToken(String token, JwtTokenType jwtTokenType) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("Token expired: {}", ex.getMessage());
            throw new TokenAuthenticationException(jwtTokenType.getValue() + " expired", ex);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token: {}", e.getMessage());
            throw new TokenAuthenticationException("Invalid " + jwtTokenType.getValue(), e);
        }
    }

    // JWT에서 사용자 이름 추출
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰에서 Authentication 객체 추출
    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token);
        return new UsernamePasswordAuthenticationToken(username, "", Collections.emptyList());
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = claims.get("userId", Long.class);
        return userId != null ? userId : 0;
    }

    public JwtAuthenticationResponseDto renewAccessToken(HttpServletRequest request) {
        try {
            String refreshToken = resolveToken(request, JwtTokenType.REFRESH.getValue());
            if (refreshToken == null) {
                throw new TokenAuthenticationException("Refresh token not found");
            }

            validateToken(refreshToken, JwtTokenType.REFRESH);

            Authentication authentication = getAuthentication(refreshToken);
            long userId = getUserIdFromToken(refreshToken);

            String newAccessToken = generateToken(authentication, userId, JwtTokenType.ACCESS);
            String newRefreshToken = generateToken(authentication, userId, JwtTokenType.REFRESH);

            return new JwtAuthenticationResponseDto(newAccessToken, newRefreshToken, "Tokens renewed successfully");
        } catch (TokenAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new TokenAuthenticationException("Failed to renew tokens: " + e.getMessage(), e);
        }
    }

}