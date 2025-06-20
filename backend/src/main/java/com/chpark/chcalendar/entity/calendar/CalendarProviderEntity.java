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
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", referencedColumnName = "id")
    private CalendarEntity calendar;

    @Column(name = "provider_id")
    private String providerId;

    @Column
    private String provider;

    @Builder
    public CalendarProviderEntity(CalendarEntity calendar, String providerId, String provider) {
        this.calendar = calendar;
        this.providerId = providerId;
        this.provider = provider;
    }
}
