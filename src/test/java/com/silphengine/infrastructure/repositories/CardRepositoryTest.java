package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.infrastructure.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


public class CardRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ExpansionRepository expansionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_shouldThrowException_whenExternalIdAlreadyExists() {

        // Given
        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        String duplicateId = "duplicateId";
        Card firstCard = createDefaultCard(duplicateId, "Magikarp", expansion);
        cardRepository.saveAndFlush(firstCard);

        Card secondCard = createDefaultCard(duplicateId, "Magikarp", expansion);

        // When & Then
        assertThatThrownBy(() -> cardRepository.saveAndFlush(secondCard))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByExternalId_shouldFindCard_whenCardExists() {

        // Given
        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        String cardExternalId = "sv02-203";
        String cardName = "Magikarp";

        Card card = createDefaultCard(cardExternalId, cardName, expansion);
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
        Expansion expansion = createDefaultExpansion(expansionExternalId);
        expansionRepository.save(expansion);

        Card card1 = createDefaultCard("sv02-203", "Magikarp", expansion);
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
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Card> foundCards = cardRepository.findByExpansion_ExternalId(expansionExternalId, pageable);

        // Then
        assertThat(foundCards).isNotEmpty();
        assertThat(foundCards).hasSize(2);
    }

    @Test
    void shouldUpdateBasicCardDetails_whenModified() {

        // Given
        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.saveAndFlush(card);
        UUID cardId = card.getId();

        entityManager.clear();

        // When
        Card managedCard = cardRepository.findById(cardId).orElseThrow();

        managedCard.updateDetails(
                "Iono",
                "Special Illustration Rare",
                CardCategory.TRAINER,
                List.of(),
                "https://example.com/new-image.png",
                "G"
        );

        // Should update card automatically
        entityManager.flush();

        entityManager.clear();

        // Then:
        Card updatedCard = cardRepository.findById(cardId).orElseThrow();

        assertThat(updatedCard.getName()).isEqualTo("Iono");
        assertThat(updatedCard.getRarity()).isEqualTo("Special Illustration Rare");
    }

    @Test
    void shouldPersistCardTypesChanges_whenTypesAreUpdatedViaDomainMethod() {

        // Given
        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.saveAndFlush(card);
        UUID cardId = card.getId();

        entityManager.clear();

        // When
        Card managedCard = cardRepository.findById(cardId).orElseThrow();

        managedCard.updateDetails(
                managedCard.getName(),
                managedCard.getRarity(),
                managedCard.getCardCategory(),
                List.of(CardType.WATER, CardType.DRAGON),
                managedCard.getImageUrl(),
                managedCard.getRegulationMark()
        );

        // Should update card automatically
        entityManager.flush();
        entityManager.clear();

        // Then:
        Card updatedCard = cardRepository.findById(cardId).orElseThrow();

        assertThat(updatedCard.getTypes()).hasSize(2);
        assertThat(updatedCard.getTypes()).containsExactlyInAnyOrder(CardType.WATER, CardType.DRAGON);
    }

    private Expansion createDefaultExpansion(String externalId) {
        return Expansion.builder()
                .externalId(externalId)
                .name("Paldea Evolved")
                .serieName("Scarlet & Violet")
                .releaseDate(LocalDate.of(2023, 6, 9))
                .totalCards(279)
                .logoUrl("https://assets.tcgdex.net/en/sv/sv02/logo")
                .build();
    }

    private Card createDefaultCard(String externalId, String name, Expansion expansion) {

        return Card.builder()
                .externalId(externalId)
                .name(name)
                .imageUrl("https://assets.tcgdex.net/en/sv/sv02/203/high.png")
                .types(List.of(CardType.WATER))
                .expansion(expansion)
                .rarity("Illustration rare")
                .cardCategory(CardCategory.POKEMON)
                .build();
    }

}
