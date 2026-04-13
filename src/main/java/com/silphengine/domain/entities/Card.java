package com.silphengine.domain.entities;

import com.silphengine.domain.enums.CardCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
@ToString
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "card_types", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "types")
    private List<String> types;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expansion_id", nullable = false)
    Expansion expansion;

    @Column(name = "rarity", nullable = false)
    private String rarity;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private CardCategory cardCategory;

}
