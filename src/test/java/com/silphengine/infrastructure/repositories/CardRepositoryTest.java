package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.infrastructure.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class CardRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ExpansionRepository expansionRepository;

    @Test
    void findByExternalId_shouldFindCard_whenCardExists() {

        // Given
        Expansion expansion = Expansion.builder()
                .externalId("sv02")
                .name("Paldea Evolved")
                .serieName("Scarlet & Violet")
                .releaseDate(LocalDate.of(2023, 6, 9))
                .totalCards(279)
                .logoUrl("https://assets.tcgdex.net/en/sv/sv02/logo")
                .build();

        expansionRepository.save(expansion);

        String cardExternalId = "sv02-203";
        String cardName = "Magikarp";

        Card card = Card.builder()
                .externalId(cardExternalId)
                .name(cardName)
                .imageUrl("https://assets.tcgdex.net/en/sv/sv02/203/high.png")
                .types(List.of(CardType.WATER))
                .expansion(expansion)
                .rarity("Illustration rare")
                .cardCategory(CardCategory.POKEMON)
                .build();

        cardRepository.save(card);

        // When
        Optional<Card> foundCard = cardRepository.findByExternalId(cardExternalId);

        // Then
        assertThat(foundCard).isPresent();
        assertThat(foundCard.get().getExternalId()).isEqualTo(cardExternalId);
        assertThat(foundCard.get().getName()).isEqualTo(cardName);
    }

    @Test
    void findByExternalId_shouldReturnEmpty_whenCardDoesNotExist() {

        // When
        Optional<Card> foundCard = cardRepository.findByExternalId("nonExistingId");

        // Then
        assertThat(foundCard).isEmpty();
    }
    
    @Test
    void findByExpansion_ExternalId_shouldFindListOfCard_whenExpansionExists() {

        // Given
        String expansionExternalId = "sv02";
        Expansion expansion = Expansion.builder()
                .externalId(expansionExternalId)
                .name("Paldea Evolved")
                .serieName("Scarlet & Violet")
                .releaseDate(LocalDate.of(2023, 6, 9))
                .totalCards(279)
                .logoUrl("https://assets.tcgdex.net/en/sv/sv02/logo")
                .build();

        expansionRepository.save(expansion);

        Card card1 = Card.builder()
                .externalId("sv02-203")
                .name("Magikarp")
                .imageUrl("https://assets.tcgdex.net/en/sv/sv02/203/high.png")
                .types(List.of(CardType.WATER))
                .expansion(expansion)
                .rarity("Illustration rare")
                .cardCategory(CardCategory.POKEMON)
                .build();

        cardRepository.save(card1);
        
        Card card2 = Card.builder()
                .externalId("sv02-269")
                .name("Iono")
                .imageUrl("https://assets.tcgdex.net/en/sv/sv02/269/high.png")
                .types(List.of())
                .expansion(expansion)
                .rarity("Special Illustration rare")
                .cardCategory(CardCategory.TRAINER)
                .build();

        cardRepository.save(card2);
        
        // When
        List<Card> foundCards = cardRepository.findByExpansion_ExternalId(expansionExternalId);

        // Then
        assertThat(foundCards).isNotEmpty();
        assertThat(foundCards).hasSize(2);
    }

}
