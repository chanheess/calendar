package com.chpark.chcalendar.security;

import com.chpark.chcalendar.dto.security.JwtAuthenticationResponseDto;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.exception.authentication.TokenAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
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

    // JWT 토큰 생성
    public String generateToken(Authentication authentication, long userId, JwtTokenType tokenType) {
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
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
            throw new TokenAuthenticationException(jwtTokenType.getValue() + " expired", ex);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
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
        String refreshToken = resolveToken(request, JwtTokenType.REFRESH.getValue());
        validateToken(refreshToken, JwtTokenType.REFRESH);

        Authentication authentication = getAuthentication(refreshToken);
        long userId = getUserIdFromToken(refreshToken);

        String newAccessToken = generateToken(authentication, userId, JwtTokenType.ACCESS);
        String newRefreshToken = generateToken(authentication, userId, JwtTokenType.REFRESH);

        return new JwtAuthenticationResponseDto(newAccessToken, newRefreshToken, "Tokens renewed successfully");
    }

}