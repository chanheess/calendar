package com.chpark.chcalendar.security;

import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.repository.user.UserRepository;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Date;

@RequiredArgsConstructor
@Component
public class OAuth2JwtTokenProvider {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public String generateToken(String email, JwtTokenType tokenType) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(
                () -> new OAuth2AuthenticationException("존재하지 않는 사용자입니다.")
        );

        long expiration = switch (tokenType) {
            case ACCESS -> jwtTokenProvider.getEXPIRATION_TIME();
            case REFRESH -> jwtTokenProvider.getREFRESH_EXPIRATION_TIME();
        };

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userEntity.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(jwtTokenProvider.getKey(), jwtTokenProvider.getSignatureAlgorithm())
                .compact();
    }
}
