package com.chpark.calendar.entity;

import com.chpark.calendar.dto.UserDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Entity
@NoArgsConstructor
@Table(name="user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    public UserEntity(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public UserEntity(UserDto.RegisterRequest request, PasswordEncoder passwordEncoder) {
        this.email = request.getEmail();
        this.password = passwordEncoder.encode(request.getPassword());
        this.nickname = request.getNickname();
    }

    public boolean checkPassword(String plainPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(plainPassword, this.password);
    }
}
