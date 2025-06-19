package com.chpark.chcalendar.entity.calendar;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="calendar_external")
public class CalendarExternalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", referencedColumnName = "id")
    private CalendarEntity calendar;

    @Column(name = "external_id")
    private String externalId;

    @Column
    private String provider;

    @Builder
    public CalendarExternalEntity(CalendarEntity calendar, String externalId, String provider) {
        this.calendar = calendar;
        this.externalId = externalId;
        this.provider = provider;
    }
}
