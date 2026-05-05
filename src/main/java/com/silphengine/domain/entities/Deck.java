package com.silphengine.domain.entities;

import com.silphengine.domain.constants.CardRarities;
import com.silphengine.domain.enums.CardCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

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

    public void evaluateLegality(List<String> standardValidMarks) {
        this.isLegal = hasCorrectDeckSize() 
                && hasValidAceSpecCount() 
                && respectsFourCopyRule() 
                && hasValidRegulationMarks(standardValidMarks);
    }

    private boolean hasCorrectDeckSize() {
        int totalCards = this.cards.stream()
                .mapToInt(DeckCard::getQuantity)
                .sum();
        return totalCards == 60;
    }

    private boolean hasValidAceSpecCount() {
        int totalAceSpecs = this.cards.stream()
                .filter(dc -> CardRarities.ACE_SPEC.equals(dc.getCard().getRarity()))
                .mapToInt(DeckCard::getQuantity)
                .sum();
        return totalAceSpecs <= 1;
    }

    private boolean respectsFourCopyRule() {
        Map<String, Integer> copiesByName = new HashMap<>();
        for (DeckCard deckCard : cards) {
            Card card = deckCard.getCard();
            if (!card.isBasicEnergy()) {
                int copies = copiesByName.merge(card.getName(), deckCard.getQuantity(), Integer::sum);
                if (copies > 4) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasValidRegulationMarks(List<String> standardValidMarks) {
        for (DeckCard deckCard : cards) {
            Card card = deckCard.getCard();
            if (!card.isBasicEnergy()) {
                String regMark = card.getRegulationMark();
                if (regMark == null || !standardValidMarks.contains(regMark)) {
                    return false;
                }
            }
        }
        return true;
    }
}
