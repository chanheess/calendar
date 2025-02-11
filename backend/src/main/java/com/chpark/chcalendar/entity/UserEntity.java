package com.chpark.chcalendar.entity;

import com.chpark.chcalendar.dto.UserDto;
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

    public boolean checkPassword(String plainPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(plainPassword, this.password);
    }

    public void changePassword(String plainPassword, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(plainPassword);
    }
}
