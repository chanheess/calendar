package com.chpark.chcalendar.utility;

import com.chpark.chcalendar.enumClass.JwtTokenType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class CookieUtility {

    public static void setCookie(JwtTokenType tokenType, String token, long maxAge, HttpServletResponse response) {
        setCookie(tokenType.getValue(), token, maxAge, response);
    }

    public static void setCookie(String tokenType, String token, long maxAge, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(tokenType, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static String getToken(HttpServletRequest request, JwtTokenType type) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (type.getValue().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
