package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.*;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.enums.Role;
import com.silphengine.infrastructure.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DeckRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpansionRepository expansionRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByOwnerId_shouldReturnPageOfDecks_whenUserExists() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");

        userRepository.save(user);

        Deck deck = createDefaultDeck("Eevee Box", user);

        user.addDeck(deck);

        deckRepository.save(deck);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Deck> foundDecks = deckRepository.findByOwnerId(user.getId(), pageable);

        // Then
        assertThat(foundDecks).isNotEmpty();
        assertThat(foundDecks).hasSize(1);
        assertThat(foundDecks.getContent().getFirst().getOwner().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByOwnerId_shouldReturnEmptyList_whenUserDoesNotExists() {

        // Given
        UUID nonExistingId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Deck> foundDecks = deckRepository.findByOwnerId(nonExistingId, pageable);

        // Then
        assertThat(foundDecks).isEmpty();
    }

    @Test
    void findByOwnerIdAndName_shouldReturnDeck_whenBothExists() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");

        userRepository.save(user);

        String deckName = "Eevee Box";
        Deck deck = createDefaultDeck(deckName, user);

        user.addDeck(deck);

        deckRepository.save(deck);

        // When
        Optional<Deck> foundDeck = deckRepository.findByOwnerIdAndName(user.getId(), deckName);

        // Then
        assertThat(foundDeck).isPresent();
        assertThat(foundDeck.get().getOwner().getId()).isEqualTo(user.getId());
        assertThat(foundDeck.get().getName()).isEqualTo(deckName);
    }

    @Test
    void findByOwnerIdAndName_shouldReturnEmpty_whenUserDoesNotExists() {

        // Given
        UUID nonExistingId = UUID.randomUUID();

        // When
        Optional<Deck> foundDeck = deckRepository.findByOwnerIdAndName(nonExistingId, "nonExistingDeck");

        // Then
        assertThat(foundDeck).isEmpty();
    }

    @Test
    void findByOwnerIdAndName_shouldReturnEmpty_whenDeckDoesNotExists() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");

        userRepository.save(user);

        // When
        Optional<Deck> foundDeck = deckRepository.findByOwnerIdAndName(user.getId(), "nonExistingDeck");

        // Then
        assertThat(foundDeck).isEmpty();
    }

    @Test
    void shouldUpdateDeckAutomatically_whenEntityIsModified_dueToDirtyChecking() {

        // Given
        User user = createDefaultUser("dirtyUser", "dirty@test.com");
        userRepository.saveAndFlush(user);

        Deck deck = createDefaultDeck("Eevee Box", user);
        user.addDeck(deck);
        deckRepository.saveAndFlush(deck);

        UUID deckId = deck.getId();

        entityManager.clear();

        // When
        Deck managedDeck = deckRepository.findById(deckId).orElseThrow();

        managedDeck.updateDetails("Absol Box", false);

        entityManager.flush();
        entityManager.clear();

        // Then
        Deck updatedDeck = deckRepository.findById(deckId).orElseThrow();

        assertThat(updatedDeck.getName()).isEqualTo("Absol Box");
        assertThat(updatedDeck.getIsLegal()).isFalse();
    }

    @Test
    void shouldRemoveCardFromDatabase_whenCardIsRemovedFromDeck() {

        // Given
        User user = createDefaultUser("orphanUser", "orphan@test.com");
        userRepository.saveAndFlush(user);

        Expansion expansion = Expansion.builder()
                .externalId("sv08.5")
                .name("Prismatic Evolutions")
                .serieName("Scarlet & Violet")
                .releaseDate(LocalDate.of(2025, 1, 17))
                .totalCards(180)
                .build();

        expansionRepository.saveAndFlush(expansion);

        Card card = Card.builder()
                .externalId("sv08.5-075")
                .name("Eevee ex")
                .imageUrl("https://assets.tcgdex.net/en/sv/sv08.5/075/high.png")
                .types(List.of(CardType.COLORLESS))
                .expansion(expansion)
                .rarity("Double rare")
                .cardCategory(CardCategory.POKEMON)
                .build();

        cardRepository.saveAndFlush(card);

        Deck deck = createDefaultDeck("Eevee Box", user);
        user.addDeck(deck);
        deckRepository.saveAndFlush(deck);

        DeckCard deckCard = DeckCard.builder()
                .card(card)
                .deck(deck)
                .quantity(1)
                .build();

        deck.addCard(deckCard);
        deckRepository.saveAndFlush(deck);

        UUID deckId = deck.getId();
        UUID baseCardId = card.getId();
        UUID deckCardId = deck.getCards().getFirst().getId();

        entityManager.clear();

        // When
        Deck savedDeck = deckRepository.findById(deckId).orElseThrow();
        DeckCard cardToRemove = savedDeck.getCards().getFirst();

        savedDeck.removeCard(cardToRemove);

        deckRepository.saveAndFlush(savedDeck);
        entityManager.clear();

        // Then
        DeckCard deletedDeckCard = entityManager.find(DeckCard.class, deckCardId);
        assertThat(deletedDeckCard).isNull();

        Card survivingBaseCard = entityManager.find(Card.class, baseCardId);
        assertThat(survivingBaseCard).isNotNull();
        assertThat(survivingBaseCard.getName()).isEqualTo("Eevee ex");
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

    private Deck createDefaultDeck(String deckName, User owner) {
        return Deck.builder()
                .name(deckName)
                .owner(owner)
                .cards(new ArrayList<>())
                .isLegal(true)
                .build();
    }

}
