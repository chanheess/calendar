package com.chpark.chcalendar.entity;

import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.exception.authentication.PasswordAuthenticationException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    public static UserEntity createWithEncodedPassword(UserDto.RegisterRequest request, PasswordEncoder passwordEncoder) {
        return UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();
    }

    public boolean checkPasswordsMatch(String plainPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(plainPassword, this.password);
    }

    public static void validatePassword(String plainPassword) {
        int passwordMin = 8;
        int passwordMax = 100;

        if (plainPassword.length() < passwordMin || plainPassword.length() > passwordMax) {
            throw new PasswordAuthenticationException(String.format("비밀번호는 %d ~ %d자 사이여야 합니다.", passwordMin, passwordMax));
        }

        if (!plainPassword.matches(".*[0-9].*")) {
            throw new PasswordAuthenticationException("비밀번호에 숫자가 하나 이상 포함되어야 합니다.");
        }

        if (!(plainPassword.matches(".*[a-z].*") || plainPassword.matches(".*[A-Z].*"))) {
            throw new PasswordAuthenticationException("비밀번호에 대문자 또는 소문자가 하나 이상 포함되어야 합니다.");
        }

        if (!plainPassword.matches(".*[!\"#$%&'()*+,-./:;<=>?@\\[\\]^_`{|}~].*")) {
            throw new PasswordAuthenticationException("비밀번호에 특수문자가 하나 이상 포함되어야 합니다.");
        }
    }

    public void changePassword(String plainPassword, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(plainPassword);
    }
}
