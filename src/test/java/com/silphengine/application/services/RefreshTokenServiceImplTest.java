package com.silphengine.application.services;

import com.silphengine.domain.entities.RefreshToken;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.exceptions.TokenRefreshException;
import com.silphengine.infrastructure.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenServiceImpl refreshTokenService;

    private User user;
    private RefreshToken refreshToken;
    private String testToken;
    private Long refreshTokenDurationMs;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenServiceImpl(refreshTokenRepository);
        refreshTokenDurationMs = 600000L;

        try {

            Field field = RefreshTokenServiceImpl.class.getDeclaredField("refreshTokenDurationMs");
            field.setAccessible(true);
            ReflectionUtils.setField(field, refreshTokenService, refreshTokenDurationMs);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to set refreshTokenDurationMs via reflection", e);
        }

        user = User.builder()
                .id(UUID.randomUUID())
                .nickname("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        testToken = UUID.randomUUID().toString();
        refreshToken = RefreshToken.builder()
                .id(1L)
                .token(testToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
    }

    @Test
    void findByToken_shouldReturnRefreshToken_whenTokenExists() {

        // Given
        when(refreshTokenRepository.findByToken(testToken)).thenReturn(Optional.of(refreshToken));

        // When
        Optional<RefreshToken> result = refreshTokenService.findByToken(testToken);

        // Then
        assertTrue(result.isPresent());
        assertEquals(refreshToken, result.get());
        verify(refreshTokenRepository, times(1)).findByToken(testToken);
        verifyNoMoreInteractions(refreshTokenRepository);
    }

    @Test
    void findByToken_shouldReturnEmptyOptional_whenTokenDoesNotExist() {

        // Given
        String nonExistentToken = "non-existent-token";
        when(refreshTokenRepository.findByToken(nonExistentToken)).thenReturn(Optional.empty());

        // When
        Optional<RefreshToken> result = refreshTokenService.findByToken(nonExistentToken);

        // Then
        assertFalse(result.isPresent());
        verify(refreshTokenRepository, times(1)).findByToken(nonExistentToken);
        verifyNoMoreInteractions(refreshTokenRepository);
    }

    @Test
    void createRefreshToken_shouldCreateNewToken_whenUserHasNoExistingToken() {
        // Given
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RefreshToken createdToken = refreshTokenService.createRefreshToken(user);

        // Then
        assertNotNull(createdToken);
        assertEquals(user, createdToken.getUser());
        assertNotNull(createdToken.getToken());
        assertTrue(createdToken.getExpiryDate().isAfter(Instant.now()));
        assertTrue(createdToken.getExpiryDate().isBefore(Instant.now().plusMillis(refreshTokenDurationMs + 1000)));

        verify(refreshTokenRepository, times(1)).findByUser(user);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        verifyNoMoreInteractions(refreshTokenRepository);
    }

    @Test
    void createRefreshToken_shouldUpdateExistingToken_whenUserHasExistingToken() {
        // Given
        RefreshToken existingToken = RefreshToken.builder()
                .id(3L)
                .token("old-token")
                .user(user)
                .expiryDate(Instant.now().minusSeconds(3600))
                .build();
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RefreshToken updatedToken = refreshTokenService.createRefreshToken(user);

        // Then
        assertNotNull(updatedToken);
        assertEquals(existingToken.getId(), updatedToken.getId());
        assertEquals(user, updatedToken.getUser());
        assertNotEquals("old-token", updatedToken.getToken());
        assertTrue(updatedToken.getExpiryDate().isAfter(Instant.now()));

        verify(refreshTokenRepository, times(1)).findByUser(user);
        verify(refreshTokenRepository, times(1)).save(existingToken);
        verifyNoMoreInteractions(refreshTokenRepository);
    }

    @Test
    void verifyExpiration_shouldReturnToken_whenTokenIsNotExpired() {

        // When
        RefreshToken verifiedToken = refreshTokenService.verifyExpiration(refreshToken);

        // Then
        assertNotNull(verifiedToken);
        assertEquals(refreshToken, verifiedToken);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verifyNoMoreInteractions(refreshTokenRepository);
    }

    @Test
    void verifyExpiration_shouldThrowTokenRefreshException_whenTokenIsExpired() {
        // Given
        refreshToken.setExpiryDate(Instant.now().minusSeconds(60));

        // When & Then
        TokenRefreshException exception = assertThrows(TokenRefreshException.class, () ->
                refreshTokenService.verifyExpiration(refreshToken));

        assertEquals("Failed for token [" + refreshToken.getToken() + "]: Refresh token has expired. Please log in again.", exception.getMessage());
        verify(refreshTokenRepository, times(1)).delete(refreshToken); // Debe borrarlo
        verifyNoMoreInteractions(refreshTokenRepository);
    }

    @Test
    void deleteByToken_shouldCallRepositoryDeleteAndRemoveFromUser() {
        // Given
        String tokenToDelete = "some-token-to-delete";
        user.assignRefreshToken(refreshToken);
        when(refreshTokenRepository.findByToken(tokenToDelete)).thenReturn(Optional.of(refreshToken));

        // When
        refreshTokenService.deleteByToken(tokenToDelete);

        // Then
        assertNull(user.getRefreshToken());
        verify(refreshTokenRepository, times(1)).findByToken(tokenToDelete);
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
        verifyNoMoreInteractions(refreshTokenRepository);
    }
}