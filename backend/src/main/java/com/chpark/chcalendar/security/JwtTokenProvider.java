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

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    // JWT 액세스 토큰 생성
    public String generateAccessToken(Authentication authentication, long userId) {
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        long EXPIRATION_TIME = 60 * 60 * 1000;
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId) // 사용자 ID를 payload에 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // JWT 리프레시 토큰 생성
    public String generateRefreshToken(Authentication authentication, long userId) {
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        long EXPIRATION_TIME = 14 * 24 * 60 * 60 * 1000;
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId) // 사용자 ID를 payload에 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // 쿠키에서 JWT 토큰 추출
    public String resolveToken(HttpServletRequest request, String tokenName) {
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

    public int getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Integer userId = claims.get("userId", Integer.class);
        return userId != null ? userId : 0;
    }

    public JwtAuthenticationResponseDto renewAccessToken(HttpServletRequest request) {
        String refreshToken = resolveToken(request, JwtTokenType.REFRESH.getValue());
        validateToken(refreshToken, JwtTokenType.REFRESH);

        Authentication authentication = getAuthentication(refreshToken);
        long userId = getUserIdFromToken(refreshToken);

        String newAccessToken = generateAccessToken(authentication, userId);
        String newRefreshToken = generateRefreshToken(authentication, userId);

        return new JwtAuthenticationResponseDto(newAccessToken, newRefreshToken, "Tokens renewed successfully");
    }

}