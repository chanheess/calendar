package com.chpark.chcalendar.entity;

import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.exception.PasswordException;
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
        int passwordMax = 20;

        if (plainPassword.length() < passwordMin || plainPassword.length() > passwordMax) {
            throw new PasswordException(String.format("Password must be between %d ~ %d characters long.", passwordMin, passwordMax));
        }

        if (!plainPassword.matches(".*[0-9].*")) {
            throw new PasswordException("Password must contain at least one digit.");
        }

        if (!(plainPassword.matches(".*[a-z].*") || plainPassword.matches(".*[A-Z].*"))) {
            throw new PasswordException("Password must contain at least one uppercase or lowercase letter.");
        }

        if (!plainPassword.matches(".*[!\"#$%&'()*+,-./:;<=>?@\\[\\]^_`{|}~].*")) {
            throw new PasswordException("Password must contain at least one special character.");
        }
    }

    public void changePassword(String plainPassword, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(plainPassword);
    }
}
