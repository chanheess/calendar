package com.chpark.chcalendar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="user_firebase_token")
public class FirebaseTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "token")
    private String token;

    @Column(name = "platform")
    private String platform;

    public void setToken(String token) {
        this.token = token;
    }

    public FirebaseTokenEntity(long userId, String token, String platform) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
    }
}

