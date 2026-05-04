package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.RefreshToken;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.Role;
import com.silphengine.infrastructure.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class RefreshTokenRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;

    @Test
    void save_shouldThrowException_whenTokenAlreadyExists() {

        // Given
        User user1 = createDefaultUser("testuser", "test@mail.com");
        userRepository.save(user1);

        User user2 = createDefaultUser("testuser2", "test2@mail.com");
        userRepository.save(user2);

        String duplicateToken = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(refreshTokenDurationMs);

        RefreshToken refreshToken1 = RefreshToken.builder()
                .token(duplicateToken)
                .expiryDate(expiryDate)
                .build();
        user1.assignRefreshToken(refreshToken1);
        userRepository.saveAndFlush(user1);

        RefreshToken refreshToken2 = RefreshToken.builder()
                .token(duplicateToken)
                .expiryDate(expiryDate)
                .build();
        user2.assignRefreshToken(refreshToken2);

        // When & Then
        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_shouldThrowException_whenUserAlreadyHasAToken() {
        // Given
        User user = createDefaultUser("testuser", "test@mail.com");
        userRepository.save(user);

        RefreshToken refreshToken1 = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        user.assignRefreshToken(refreshToken1);
        userRepository.saveAndFlush(user);

        RefreshToken refreshToken2 = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user) // Asignamos vía builder
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        // When & Then
        assertThatThrownBy(() -> refreshTokenRepository.saveAndFlush(refreshToken2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByToken_shouldFindToken_whenTokenExists() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");

        String tokenString = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenString)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        user.assignRefreshToken(refreshToken);
        userRepository.saveAndFlush(user);

        // When
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken(tokenString);

        // Then
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getToken()).isEqualTo(tokenString);
        assertThat(foundToken.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByToken_shouldReturnEmpty_whenTokenDoesNotExists() {

        // When
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken("nonExistingToken");

        // Then
        assertThat(foundToken).isEmpty();
    }

    @Test
    void findByUser_shouldFindToken_whenUserExists() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");

        String tokenString = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenString)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        user.assignRefreshToken(refreshToken);
        userRepository.saveAndFlush(user);

        // When
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByUser(user);

        // Then
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getToken()).isEqualTo(tokenString);
        assertThat(foundToken.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByUser_shouldReturnEmpty_whenTokenDoesNotExists() {

        // Given
        User user = createDefaultUser("testuser", "test@user.com");
        userRepository.saveAndFlush(user);

        // When
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByUser(user);

        // Then
        assertThat(foundToken).isEmpty();
    }

    @Test
    void deleteUser_shouldDeleteAssociatedRefreshToken() {

        // Given
        User user = createDefaultUser("testuser", "test@mail.com");

        String tokenString = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenString)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        user.assignRefreshToken(refreshToken);

        userRepository.saveAndFlush(user);
        entityManager.clear();

        Optional<RefreshToken> savedToken = refreshTokenRepository.findByToken(tokenString);
        assertThat(savedToken).isPresent();

        // When
        userRepository.deleteById(user.getId());
        userRepository.flush();

        // Then
        Optional<RefreshToken> orphanToken = refreshTokenRepository.findByToken(tokenString);
        assertThat(orphanToken).isEmpty();
    }

    @Test
    void removeRefreshTokenFromUser_shouldDeleteOrphanToken() {
        // Given
        User user = createDefaultUser("testuser", "test@mail.com");

        String tokenString = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenString)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        user.assignRefreshToken(refreshToken);
        userRepository.saveAndFlush(user);
        
        entityManager.clear();

        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getRefreshToken()).isNotNull();

        // When
        savedUser.removeRefreshToken();
        userRepository.saveAndFlush(savedUser);
        
        entityManager.clear();

        // Then
        Optional<RefreshToken> orphanToken = refreshTokenRepository.findByToken(tokenString);
        assertThat(orphanToken).isEmpty();
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