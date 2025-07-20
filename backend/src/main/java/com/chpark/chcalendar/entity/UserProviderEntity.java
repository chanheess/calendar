package com.chpark.chcalendar.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_provider", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "provider"}))
public class UserProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String provider = "local";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    public UserProviderEntity(String provider, UserEntity user) {
        this.provider = provider;
        this.user = user;
    }

    @Builder
    public UserProviderEntity(Long id, String provider, UserEntity user) {
        this.id = id;
        this.provider = provider;
        this.user = user;
    }
}







