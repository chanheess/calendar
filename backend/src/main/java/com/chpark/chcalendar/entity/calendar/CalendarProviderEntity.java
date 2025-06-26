package com.chpark.chcalendar.entity.calendar;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="calendar_provider")
public class CalendarProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", referencedColumnName = "id")
    private CalendarEntity calendar;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column
    private String provider;

    @Column(name = "sync_token")
    private String syncToken;

    @Column
    private String status = "read";

    @Builder
    public CalendarProviderEntity(String syncToken, String provider, String providerId, String status, CalendarEntity calendar) {
        this.calendar = calendar;
        this.providerId = providerId;
        this.provider = provider;
        this.syncToken = syncToken;
        this.status = status;
    }
}
