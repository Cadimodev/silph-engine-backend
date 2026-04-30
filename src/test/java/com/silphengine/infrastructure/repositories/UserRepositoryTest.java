package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Deck;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.Role;
import com.silphengine.infrastructure.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.shouldHaveThrown;


public class UserRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_shouldThrowException_whenNicknameAlreadyExists() {

        // Given
        String duplicateNickname = "duplicateNickname";
        User firstUser = createDefaultUser(duplicateNickname, "test1@user.com");
        userRepository.saveAndFlush(firstUser);

        User secondUser = createDefaultUser(duplicateNickname, "test2@user.com");

        // When & Then
        assertThatThrownBy(() -> userRepository.saveAndFlush(secondUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_shouldThrowException_whenEmailAlreadyExists() {

        // Given
        String duplicateEmail = "duplicateEmail@user.com";
        User firstUser = createDefaultUser("testuser1", duplicateEmail);
        userRepository.saveAndFlush(firstUser);

        User secondUser = createDefaultUser("testuser2", duplicateEmail);

        // When & Then
        assertThatThrownBy(() -> userRepository.saveAndFlush(secondUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_shouldAutoGenerateCreatedAt_whenNewUserIsSaved() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");

        // When
        User savedUser = userRepository.saveAndFlush(user);

        // Then
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void findByNickname_shouldFindUser_whenUserExists() {

        // Given
        String nickname = "testuser";
        User user = createDefaultUser(nickname, "test@user.com");

        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByNickname(nickname);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNickname()).isEqualTo(nickname);
    }

    @Test
    void findByNickname_shouldReturnEmpty_whenUserDoesNotExists() {

        // When
        Optional<User> foundUser = userRepository.findByNickname("nonExistingNickname");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByEmail_shouldFindUser_whenUserExists() {

        // Given
        String email = "test@user.com";
        User user = createDefaultUser("testuser", email);

        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByEmail(email);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenUserDoesNotExists() {

        // When
        Optional<User> foundUser = userRepository.findByEmail("nonExistingEmail@test.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByNicknameOrEmail_shouldFindUser_whenUserExists() {

        // Given
        String nickname = "testuser";
        String email = "test@user.com";
        User user = createDefaultUser(nickname, email);

        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByNicknameOrEmail(nickname, email);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNickname()).isEqualTo(nickname);
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
    }

    @Test
    void findByNicknameOrEmail_shouldReturnEmpty_whenUserDoesNotExists() {

        // When
        Optional<User> foundUser = userRepository.findByNicknameOrEmail("nonExistingNickname", "nonExistingEmail@test.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void existsByNickname_shouldFindUser_whenUserExists() {

        // Given
        String nickname = "testuser";
        User user = createDefaultUser(nickname, "test@user.com");

        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByNickname(nickname);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByNickname_shouldReturnFalse_whenUserDoesNotExists() {

        // When
        boolean exists = userRepository.existsByNickname("nonExistingNickname");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_shouldFindUser_whenUserExists() {

        // Given
        String email = "test@user.com";
        User user = createDefaultUser("testuser", email);

        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmail(email);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenUserDoesNotExists() {

        // When
        boolean exists = userRepository.existsByEmail("nonExistingEmail@test.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldRemoveDeckFromDatabase_whenDeckIsRemovedFromUserCollection() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");
        Deck deck = Deck.builder()
                .name("Eevee Box")
                .owner(user)
                .cards(new ArrayList<>())
                .isLegal(false)
                .build();

        user.addDeck(deck);

        userRepository.saveAndFlush(user);

        UUID userId = user.getId();
        UUID deckId = deck.getId();

        entityManager.clear();

        // When
        User savedUser = userRepository.findById(userId).orElseThrow();
        Deck deckToRemove = savedUser.getDecks().getFirst();
        savedUser.removeDeck(deckToRemove);

        userRepository.saveAndFlush(savedUser);
        entityManager.clear();

        Optional<Deck> deletedDeck = deckRepository.findById(deckId);

        assertThat(deletedDeck).isEmpty();
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
}
