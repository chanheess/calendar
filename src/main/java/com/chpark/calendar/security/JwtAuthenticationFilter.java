package com.chpark.calendar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(request);
        // 요청의 URL과 클라이언트 IP 로그 출력
        String requestURL = request.getRequestURL().toString();
        String clientIP = request.getRemoteAddr();
        System.out.println("요청 URL: " + requestURL);
        System.out.println("클라이언트 IP: " + clientIP);
        System.out.println("서버에서 받은 토큰: " + token); // 로그로 토큰 확인

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("JWT 인증 성공: " + auth.getName());
        } else {
            System.out.println("JWT 인증 실패 또는 토큰 없음.");
        }
        filterChain.doFilter(request, response);
    }

}
