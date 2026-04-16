package com.silphengine.domain.entities;

import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
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
@ToString
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection(targetClass = CardType.class)
    @CollectionTable(name = "card_types", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "type_name")
    @Enumerated(EnumType.STRING)
    @ToString.Exclude
    private List<CardType> types;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expansion_id", nullable = false)
    @ToString.Exclude
    private Expansion expansion;

    @Column(nullable = false)
    private String rarity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardCategory cardCategory;

    public void updateDetails(String name, String rarity, CardCategory cardCategory, List<CardType> types, String imageUrl) {
        this.name = name;
        this.rarity = rarity;
        this.cardCategory = cardCategory;
        this.types = types;
        this.imageUrl = imageUrl;
    }
}
