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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 10)
    private String provider = "local";

    @Column(name = "provider_email")
    private String providerEmail;

    public UserProviderEntity(String provider, UserEntity user, String providerEmail) {
        this.provider = provider;
        this.user = user;
        this.providerEmail = providerEmail;
    }

    @Builder
    public UserProviderEntity(Long id, String provider, UserEntity user, String providerEmail) {
        this.id = id;
        this.provider = provider;
        this.user = user;
        this.providerEmail = providerEmail;
    }
}







