package com.silphengine.application.services;

import com.silphengine.domain.services.RefreshTokenService;
import com.silphengine.domain.entities.RefreshToken;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.exceptions.TokenRefreshException;
import com.silphengine.infrastructure.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> RefreshToken.builder().build());

        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        token.setToken(UUID.randomUUID().toString());

        user.assignRefreshToken(token);

        return refreshTokenRepository.save(token);
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            User user = token.getUser();
            if (user != null) {
                user.removeRefreshToken();
            }
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token has expired. Please log in again.");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            User user = refreshToken.getUser();
            if (user != null) {
                user.removeRefreshToken();
            }
            refreshTokenRepository.delete(refreshToken);
        });
    }
}
