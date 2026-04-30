package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.*;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.enums.Role;
import com.silphengine.infrastructure.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class DeckCardRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    private DeckCardRepository deckCardRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ExpansionRepository expansionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByDeckId_shouldReturnListOfDeckCards_whenDeckExists() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");
        userRepository.save(user);

        Deck deck = createDefaultDeck("Eevee Box", user);
        user.addDeck(deck);
        deckRepository.save(deck);

        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.save(card);

        int deckCardQuantity = 4;
        DeckCard deckCard = createDefaultDeckCard(card, deck, deckCardQuantity);
        deck.addCard(deckCard);
        deckCardRepository.save(deckCard);

        // When
        List<DeckCard> deckCardList = deckCardRepository.findByDeckId(deck.getId());

        // Then
        assertThat(deckCardList).isNotEmpty();
        assertThat(deckCardList).hasSize(1);
        assertThat(deckCardList.getFirst().getId()).isEqualTo(deckCard.getId());
        assertThat(deckCardList.getFirst().getQuantity()).isEqualTo(deckCardQuantity);
    }

    @Test
    void findByDeckId_shouldReturnEmpty_whenDeckDoesNotExists() {

        // Given
        UUID nonExistingId = UUID.randomUUID();

        // When
        List<DeckCard> deckCardList = deckCardRepository.findByDeckId(nonExistingId);

        // Then
        assertThat(deckCardList).isEmpty();
    }

    @Test
    void shouldUpdateDeckCardAutomatically_whenEntityIsModified_dueToDirtyChecking() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");
        userRepository.save(user);

        Deck deck = createDefaultDeck("Eevee Box", user);
        user.addDeck(deck);
        deckRepository.save(deck);

        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.save(card);

        DeckCard deckCard = createDefaultDeckCard(card, deck, 4);
        deck.addCard(deckCard);
        deckCardRepository.saveAndFlush(deckCard);

        UUID deckCardId = deckCard.getId();

        entityManager.clear();

        // When
        DeckCard managedDeckCard = deckCardRepository.findById(deckCardId).orElseThrow();
        managedDeckCard.changeQuantity(3);

        entityManager.flush();
        entityManager.clear();

        // Then
        DeckCard updatedDeckCard = deckCardRepository.findById(deckCardId).orElseThrow();

        assertThat(updatedDeckCard.getQuantity()).isEqualTo(3);
    }

    @Test
    void save_shouldThrowException_whenDeckAndCardCombinationAlreadyExists() {

        // Given
        User user = createDefaultUser("usertest", "user@test.com");
        userRepository.save(user);

        Deck deck = createDefaultDeck("Eevee Box", user);
        user.addDeck(deck);
        deckRepository.save(deck);

        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.save(card);

        DeckCard firstDeckCard = createDefaultDeckCard(card, deck, 4);
        deck.addCard(firstDeckCard);
        deckCardRepository.saveAndFlush(firstDeckCard);
        
        DeckCard duplicateDeckCard = createDefaultDeckCard(card, deck, 1);
        deck.addCard(duplicateDeckCard);

        // When & Then
        assertThatThrownBy(() -> deckCardRepository.saveAndFlush(duplicateDeckCard))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Deck createDefaultDeck(String deckName, User owner) {
        return Deck.builder()
                .name(deckName)
                .owner(owner)
                .cards(new ArrayList<>())
                .isLegal(true)
                .build();
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

    private User createDefaultUser(String nickname, String email) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .password("Password1234!")
                .role(Role.USER)
                .collection(new ArrayList<>())
                .decks(new ArrayList<>())
                .build();
    }

    private DeckCard createDefaultDeckCard(Card card, Deck deck, int quantity) {
        return DeckCard.builder()
                .card(card)
                .deck(deck)
                .quantity(quantity)
                .build();
    }
}
