package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.entities.InventoryCard;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.*;
import com.silphengine.infrastructure.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class InventoryCardRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    private InventoryCardRepository inventoryCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpansionRepository expansionRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_shouldThrowException_whenCombinationOfCardConditionFinishAlreadyExists() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");
        userRepository.save(user);

        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.save(card);

        InventoryCard inventoryCard1 = InventoryCard.builder()
                .owner(user)
                .card(card)
                .quantity(1)
                .cardCondition(CardCondition.NEAR_MINT)
                .cardFinish(CardFinish.NORMAL)
                .build();

        user.addInventoryCard(inventoryCard1);
        inventoryCardRepository.saveAndFlush(inventoryCard1);

        InventoryCard inventoryCard2 = InventoryCard.builder()
                .owner(user)
                .card(card)
                .quantity(1)
                .cardCondition(CardCondition.NEAR_MINT)
                .cardFinish(CardFinish.NORMAL)
                .build();
        user.addInventoryCard(inventoryCard2);

        // When & Then
        assertThatThrownBy(() -> inventoryCardRepository.saveAndFlush(inventoryCard2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByOwnerId_shouldReturnPageOfInventoryCards_whenUserExists() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");
        userRepository.save(user);

        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.save(card);

        InventoryCard inventoryCard = InventoryCard.builder()
                .owner(user)
                .card(card)
                .quantity(1)
                .cardCondition(CardCondition.NEAR_MINT)
                .cardFinish(CardFinish.NORMAL)
                .build();

        user.addInventoryCard(inventoryCard);
        inventoryCardRepository.saveAndFlush(inventoryCard);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InventoryCard> inventoryCardPage = inventoryCardRepository.findByOwnerId(user.getId(), pageable);

        // Then
        assertThat(inventoryCardPage).isNotEmpty();
        assertThat(inventoryCardPage.getContent().getFirst().getId()).isEqualTo(inventoryCard.getId());

    }

    @Test
    void findByOwnerId_shouldReturnEmptyList_whenUserDoesNotExists() {

        // Given
        UUID nonExistingUserId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<InventoryCard> inventoryCardPage = inventoryCardRepository.findByOwnerId(nonExistingUserId, pageable);

        // Then
        assertThat(inventoryCardPage).isEmpty();
    }

    @Test
    void findByOwnerIdAndCardId_shouldReturnListOfInventoryCards_whenUserAndInventoryCardExists() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");
        userRepository.save(user);

        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.save(card);

        InventoryCard inventoryCard = InventoryCard.builder()
                .owner(user)
                .card(card)
                .quantity(1)
                .cardCondition(CardCondition.NEAR_MINT)
                .cardFinish(CardFinish.NORMAL)
                .build();

        user.addInventoryCard(inventoryCard);
        inventoryCardRepository.saveAndFlush(inventoryCard);

        // When
        List<InventoryCard> foundInventoryCards = inventoryCardRepository.findByOwnerIdAndCardId(user.getId(), card.getId());

        // Then
        assertThat(foundInventoryCards).isNotEmpty();
        assertThat(foundInventoryCards.getFirst().getId()).isEqualTo(inventoryCard.getId());
    }

    @Test
    void findByOwnerIdAndCardId_shouldReturnEmptyList_whenUserAndInventoryCardDoesNotExists() {

        // Given
        UUID nonExistingUserId = UUID.randomUUID();
        UUID nonExistingInventoryCardId = UUID.randomUUID();

        // When
        List<InventoryCard> foundInventoryCards = inventoryCardRepository.findByOwnerIdAndCardId(nonExistingUserId, nonExistingInventoryCardId);

        // Then
        assertThat(foundInventoryCards).isEmpty();
    }

    @Test
    void findByIdAndOwnerId_shouldReturnInventoryCard_whenUserAndInventoryCardExist() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");
        userRepository.save(user);

        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.save(card);

        InventoryCard inventoryCard = InventoryCard.builder()
                .owner(user)
                .card(card)
                .quantity(1)
                .cardCondition(CardCondition.NEAR_MINT)
                .cardFinish(CardFinish.NORMAL)
                .build();

        user.addInventoryCard(inventoryCard);
        inventoryCard = inventoryCardRepository.saveAndFlush(inventoryCard);

        // When
        Optional<InventoryCard> foundInventoryCard = inventoryCardRepository.findByIdAndOwnerId(inventoryCard.getId(), inventoryCard.getOwner().getId());

        // Then
        assertThat(foundInventoryCard).isPresent();
        assertThat(foundInventoryCard.get().getId()).isEqualTo(inventoryCard.getId());
    }

    @Test
    void findByIdAndOwnerId_shouldReturnEmpty_whenCardDoesNotExist() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");
        userRepository.save(user);

        UUID nonExistingId = UUID.randomUUID();

        // When
        Optional<InventoryCard> foundInventoryCard = inventoryCardRepository.findByIdAndOwnerId(nonExistingId, user.getId());

        // Then
        assertThat(foundInventoryCard).isEmpty();
    }

    @Test
    void findByOwnerIdAndCardIdAndCardConditionAndCardFinish_shouldReturnInventoryCard_whenCardExist() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");
        userRepository.save(user);

        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.save(card);

        InventoryCard inventoryCard = InventoryCard.builder()
                .owner(user)
                .card(card)
                .quantity(1)
                .cardCondition(CardCondition.NEAR_MINT)
                .cardFinish(CardFinish.NORMAL)
                .build();

        user.addInventoryCard(inventoryCard);
        inventoryCard = inventoryCardRepository.saveAndFlush(inventoryCard);

        // When
        Optional<InventoryCard> foundInventoryCard = inventoryCardRepository.findByOwnerIdAndCardIdAndCardConditionAndCardFinish(
                inventoryCard.getOwner().getId(), inventoryCard.getCard().getId(), inventoryCard.getCardCondition(), inventoryCard.getCardFinish());

        // Then
        assertThat(foundInventoryCard).isPresent();
        assertThat(foundInventoryCard.get().getId()).isEqualTo(inventoryCard.getId());
    }

    @Test
    void findByOwnerIdAndCardIdAndCardConditionAndCardFinish_shouldReturnEmpty_whenCardDoesNotExist() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");
        userRepository.save(user);

        UUID nonExistingId = UUID.randomUUID();

        // When
        Optional<InventoryCard> foundInventoryCard = inventoryCardRepository.findByOwnerIdAndCardIdAndCardConditionAndCardFinish(
                user.getId(), nonExistingId, CardCondition.NEAR_MINT, CardFinish.HOLO);

        // Then
        assertThat(foundInventoryCard).isEmpty();
    }

    @Test
    void shouldUpdateInventoryCardAutomatically_whenQuantityIsModified_dueToDirtyChecking() {

        // Given
        User user = createDefaultUser("testuser", "user@test.com");
        userRepository.save(user);

        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.save(card);

        InventoryCard inventoryCard = InventoryCard.builder()
                .owner(user)
                .card(card)
                .quantity(1)
                .cardCondition(CardCondition.NEAR_MINT)
                .cardFinish(CardFinish.NORMAL)
                .build();

        user.addInventoryCard(inventoryCard);

        InventoryCard savedInventoryCard = inventoryCardRepository.saveAndFlush(inventoryCard);
        UUID inventoryCardId = savedInventoryCard.getId();

        entityManager.clear();

        // When
        InventoryCard managedInventoryCard = inventoryCardRepository.findById(inventoryCardId).orElseThrow();
        managedInventoryCard.changeQuantity(5);

        entityManager.flush();
        entityManager.clear();

        InventoryCard updatedInventoryCard = inventoryCardRepository.findById(inventoryCardId).orElseThrow();

        // Then
        assertThat(updatedInventoryCard.getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldRemoveInventoryCardFromDatabase_whenCardIsRemovedFromUserCollection() {

        // Given
        User user = createDefaultUser("orphanUser", "orphan@test.com");
        userRepository.save(user);

        Expansion expansion = createDefaultExpansion("sv02");
        expansionRepository.save(expansion);

        Card card = createDefaultCard("sv02-203", "Magikarp", expansion);
        cardRepository.save(card);

        InventoryCard inventoryCard = InventoryCard.builder()
                .owner(user)
                .card(card)
                .quantity(1)
                .cardCondition(CardCondition.NEAR_MINT)
                .cardFinish(CardFinish.NORMAL)
                .build();

        user.addInventoryCard(inventoryCard);

        InventoryCard savedInventoryCard = inventoryCardRepository.saveAndFlush(inventoryCard);
        UUID inventoryCardId = savedInventoryCard.getId();
        UUID userId = user.getId();

        entityManager.clear();

        // When
        User savedUser = userRepository.findById(userId).orElseThrow();
        InventoryCard cardToRemove = savedUser.getCollection().getFirst();

        savedUser.removeInventoryCard(cardToRemove);

        userRepository.saveAndFlush(savedUser);
        entityManager.clear();
        InventoryCard deletedInventoryCard = entityManager.find(InventoryCard.class, inventoryCardId);

        // Then
        assertThat(deletedInventoryCard).isNull();
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
}
