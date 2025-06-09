package com.chpark.chcalendar.entity;

import com.chpark.chcalendar.dto.user.UserDto;
import com.chpark.chcalendar.exception.authentication.PasswordAuthenticationException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @Builder.Default
    private Set<UserProviderEntity> providers = new HashSet<>();

    public static UserEntity createWithEncodedPassword(UserDto.RegisterRequest request, PasswordEncoder passwordEncoder) {
        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();

        UserProviderEntity provider = new UserProviderEntity();
        provider.setProvider(request.getProvider());

        user.addProvider(provider);
        return user;
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

    public void addProvider(UserProviderEntity provider) {
        provider.setUser(this);
        this.providers.add(provider);
    }
}
