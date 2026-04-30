package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Deck;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.Role;
import com.silphengine.infrastructure.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
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

    @Test
    void findByOwnerId_shouldReturnListOfDecks_whenUserExists() {

        // Given
        User user = User.builder()
                .nickname("testuser")
                .email("test@user.com")
                .password("Password1234!")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .collection(new ArrayList<>())
                .decks(new ArrayList<>())
                .build();

        userRepository.save(user);

        Deck deck = Deck.builder()
                .name("Eevee Box")
                .owner(user)
                .cards(new ArrayList<>())
                .isLegal(true)
                .build();

        user.addDeck(deck);

        deckRepository.save(deck);

        // When
        List<Deck> foundDecks = deckRepository.findByOwnerId(user.getId());

        // Then
        assertThat(foundDecks).isNotEmpty();
        assertThat(foundDecks).hasSize(1);
        assertThat(foundDecks.getFirst().getOwner().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByOwnerId_shouldReturnEmptyList_whenUserDoesNotExists() {

        // Given
        UUID nonExistingId = UUID.randomUUID();

        // When
        List<Deck> foundDecks = deckRepository.findByOwnerId(nonExistingId);

        // Then
        assertThat(foundDecks).isEmpty();
    }

    @Test
    void findByOwnerIdAndName_shouldReturnDeck_whenBothExists() {

        // Given
        User user = User.builder()
                .nickname("testuser")
                .email("test@user.com")
                .password("Password1234!")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .collection(new ArrayList<>())
                .decks(new ArrayList<>())
                .build();

        userRepository.save(user);

        String deckName = "Eevee Box";
        Deck deck = Deck.builder()
                .name(deckName)
                .owner(user)
                .cards(new ArrayList<>())
                .isLegal(true)
                .build();

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
        User user = User.builder()
                .nickname("testuser")
                .email("test@user.com")
                .password("Password1234!")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .collection(new ArrayList<>())
                .decks(new ArrayList<>())
                .build();

        userRepository.save(user);

        // When
        Optional<Deck> foundDeck = deckRepository.findByOwnerIdAndName(user.getId(), "nonExistingDeck");

        // Then
        assertThat(foundDeck).isEmpty();
    }

}
