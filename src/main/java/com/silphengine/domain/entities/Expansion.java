package com.silphengine.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expansions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
@Getter
@Setter
public class Expansion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "serie_name", nullable = false)
    private String serieName;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "total_cards", nullable = false)
    private int totalCards;

    @Column(name = "logo_url")
    private String logoUrl;
}
