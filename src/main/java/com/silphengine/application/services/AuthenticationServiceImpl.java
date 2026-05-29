package com.silphengine.application.services;

import com.silphengine.domain.dto.requests.LoginRequest;
import com.silphengine.domain.dto.requests.RefreshTokenRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.AuthResponse;
import com.silphengine.domain.entities.RefreshToken;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.exceptions.TokenRefreshException;
import com.silphengine.domain.services.AuthenticationService;
import com.silphengine.domain.services.RefreshTokenService;
import com.silphengine.domain.services.UserService;
import com.silphengine.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Override
    @Transactional
    public AuthResponse register(UserRequest request) {
        
        userService.createUser(request);
        
        return login(new LoginRequest(request.nickname(), request.password()));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.nickname(), request.password())
        );

        
        User user = (User) authentication.getPrincipal();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        String accessToken = jwtService.generateAccessToken(extraClaims, user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getNickname());
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(refreshToken -> {

                    User user = refreshToken.getUser();
                    
                    var newRefreshToken = refreshTokenService.createRefreshToken(user);
                    Map<String, Object> extraClaims = new HashMap<>();
                    extraClaims.put("role", user.getRole().name());
                    var accessToken = jwtService.generateAccessToken(extraClaims, user);
                    
                    return new AuthResponse(
                            accessToken,
                            newRefreshToken.getToken(),
                            user.getNickname()
                    );
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token does not exists."));
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.refreshToken());
    }
}
