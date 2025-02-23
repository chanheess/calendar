package com.chpark.chcalendar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="user_web_push")
public class WebPushEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "p256dh_key")
    private String p256dhKey;

    @Column(name = "auth_key")
    private String authKey;

    public WebPushEntity(long userId, String endpoint, String p256dhKey, String authKey) {
        this.userId = userId;
        this.endpoint = endpoint;
        this.p256dhKey = p256dhKey;
        this.authKey = authKey;
    }
}
