package com.silphengine.infrastructure.web.controllers;

import tools.jackson.databind.json.JsonMapper;
import com.silphengine.domain.dto.requests.LoginRequest;
import com.silphengine.domain.dto.requests.RefreshTokenRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.AuthResponse;
import com.silphengine.domain.services.AuthenticationService;
import com.silphengine.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void register_shouldReturnOkAndAuthResponse_whenRequestIsValid() throws Exception {

        // Given
        UserRequest userRequest = new UserRequest("testuser", "test@example.com", "Password123!");
        AuthResponse authResponse = new AuthResponse("dummy-access-token", "dummy-refresh-token", "testuser");
        when(authenticationService.register(any(UserRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("dummy-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("dummy-refresh-token"))
                .andExpect(jsonPath("$.nickname").value("testuser"));
    }

    @Test
    void register_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {

        // Given
        UserRequest invalidUserRequest = new UserRequest("test", "not-an-email", "short");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnOkAndAuthResponse_whenCredentialsAreValid() throws Exception {

        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        AuthResponse authResponse = new AuthResponse("dummy-access-token", "dummy-refresh-token", "testuser");
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("dummy-access-token"));
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {

        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_shouldReturnOkAndNewAuthResponse_whenTokenIsValid() throws Exception {

        // Given
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("valid-refresh-token");
        AuthResponse newAuthResponse = new AuthResponse("new-access-token", "new-refresh-token", "testuser");
        when(authenticationService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(newAuthResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void logout_shouldReturnNoContent_whenRequestIsValid() throws Exception {

        // Given
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("some-refresh-token");
        doNothing().when(authenticationService).logout(any(RefreshTokenRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isNoContent());
    }
}