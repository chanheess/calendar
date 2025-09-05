package com.chpark.chcalendar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="user_firebase_token")
public class FirebaseTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Setter
    @Column(name = "token")
    private String token;

    @Column(name = "platform")
    private String platform;

    public FirebaseTokenEntity(long userId, String token, String platform) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
    }
}

