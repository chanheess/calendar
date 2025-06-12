package com.chpark.chcalendar.security;

import com.chpark.chcalendar.enumClass.JwtTokenType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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

        String googleToken = jwtTokenProvider.resolveToken(request, JwtTokenType.GOOGLE_ACCESS.getValue());
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());

        if (googleToken != null) {
            Authentication googleAuth = new UsernamePasswordAuthenticationToken("google_user", null, null);
            SecurityContextHolder.getContext().setAuthentication(googleAuth);
        } else if (token != null && jwtTokenProvider.validateToken(token, JwtTokenType.ACCESS)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars") ||
               path.equals("/swagger-ui.html") ||
               path.equals("/swagger-ui/index.html") ||
               path.contains("/swagger-ui/") ||
               path.contains("/v3/api-docs/");
    }

}
