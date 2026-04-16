package com.silphengine.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "decks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@ToString
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    private User owner;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeckCard> cards = new ArrayList<>();

    @Column(name = "is_legal", nullable = false)
    @Builder.Default
    private Boolean isLegal = false;

    public void updateDetails(String name, Boolean isLegal) {
        this.name = name;
        this.isLegal = isLegal;
    }

    protected void setOwner(User owner) {
        this.owner = owner;
    }

    public void addCard(DeckCard deckCard) {
        cards.add(deckCard);
        deckCard.setDeck(this);
    }

    public void removeCard(DeckCard deckCard) {
        cards.remove(deckCard);
        deckCard.setDeck(null);
    }
}
