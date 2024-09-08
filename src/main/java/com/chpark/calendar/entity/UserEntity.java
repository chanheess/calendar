package com.chpark.calendar.entity;

import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.enumClass.UserStatus;
import com.chpark.calendar.utility.UserStatusConverter;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Entity
@Table(name="user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Convert(converter = UserStatusConverter.class)
    @Column(name = "status")
    private UserStatus status;


    public UserEntity(UserDto.PostRequest request, PasswordEncoder passwordEncoder) {
        this.emailId = request.getEmailId();
        this.password = passwordEncoder.encode(request.getPassword());
        this.nickname = request.getNickname();
        this.status = UserStatus.ACTIVE;
    }

    public UserEntity setHashPassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
        return this;
    }

    public boolean checkPassword(String plainPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(plainPassword, this.password);
    }
}
