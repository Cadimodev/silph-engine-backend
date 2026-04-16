package com.silphengine.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/*
 * Unique constraint to ensure that each card appears only once per deck.
 * A deck should not have multiple rows for the same card; instead, the 'quantity'
 * field should be updated. This prevents data redundancy and ensures
 * consistency when calculating total deck size or card limits.
 */

@Entity
@Table(name = "deck_cards", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_deck_card_line",
                columnNames = {"deck_id", "card_id"}
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@ToString
public class DeckCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    @ToString.Exclude
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    @ToString.Exclude
    private Deck deck;

    @Column(nullable = false)
    private Integer quantity;

    public void changeQuantity(Integer newQuantity) {
        if (newQuantity > 0) {
            this.quantity = newQuantity;
        }
    }

    protected void setDeck(Deck deck) {
        this.deck = deck;
    }
}
