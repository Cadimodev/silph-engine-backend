package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.dto.requests.PasswordChangeRequest;
import com.silphengine.domain.dto.requests.UserProfileRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.UserResponse;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.exceptions.BadRequestException;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.UserService;
import com.silphengine.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private User mockUser;
    private UUID mockUserId;

    private User mockAdmin;
    private UUID mockAdminId;

    @BeforeEach
    void setUp() {

        mockUserId = UUID.randomUUID();
        mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(mockUserId);

        mockAdminId = UUID.randomUUID();
        mockAdmin = mock(User.class);
        when(mockAdmin.getId()).thenReturn(mockAdminId);

        // By default
        authenticateAsUser();
    }

    @AfterEach
    void tearDown() {

        // Clear the context after each test
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsUser() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void authenticateAsAdmin() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                mockAdmin, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createUser_shouldReturnCreatedAndUserResponse_whenAdminCreatesUser() throws Exception {

        // Given
        authenticateAsAdmin();

        String nickname = "testuser";
        String email = "user@test.com";
        String password = "Password1234!";
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        UserRequest request = new UserRequest(nickname, email, password);
        UserResponse response = new UserResponse(id, nickname, email, createdAt);

        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nickname").value(nickname));

    }

    @Test
    void createUser_shouldReturnConflict_whenNicknameAlreadyExists() throws Exception {

        // Given
        authenticateAsAdmin();

        String nickname = "existingNickname";
        String email = "user@test.com";
        String password = "Password1234!";

        UserRequest request = new UserRequest(nickname, email, password);

        when(userService.createUser(any(UserRequest.class))).thenThrow(new DuplicateResourceException("Nickname already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {

        // Given
        authenticateAsAdmin();

        String nickname = "testuser";
        String email = "existingEmail@test.com";
        String password = "Password1234!";

        UserRequest request = new UserRequest(nickname, email, password);

        when(userService.createUser(any(UserRequest.class))).thenThrow(new DuplicateResourceException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUserById_shouldReturnOkAndUserResponse_whenAdminRequestsAnyUser() throws Exception {

        // Given
        authenticateAsAdmin();

        String nickname = "testuser";
        UUID id = UUID.randomUUID();

        UserResponse response = new UserResponse(id, nickname, "user@test.com", LocalDateTime.now());

        when(userService.getUserById(eq(id))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nickname").value(nickname));

    }

    @Test
    void getUserById_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
        authenticateAsAdmin();

        UUID id = UUID.randomUUID();

        when(userService.getUserById(eq(id))).thenThrow(new ResourceNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyProfile_shouldReturnOkAndUserResponse() throws Exception {

        // Given
        UserResponse response = new UserResponse(mockUserId, "testuser", "user@test.com", LocalDateTime.now());
        when(userService.getUserById(eq(mockUserId))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockUserId.toString()));
    }

    @Test
    void getUserByNickname_shouldReturnOkAndUserResponse_whenUserExists() throws Exception {

        // Given
        String nickname = "testuser";
        String email = "user@test.com";
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        UserResponse response = new UserResponse(id, nickname, email, createdAt);

        when(userService.getUserByNickname(eq(nickname))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/users/nickname/{nickname}", nickname)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nickname").value(nickname));

    }

    @Test
    void getUserByNickname_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
        String nickname = "testuser";

        when(userService.getUserByNickname(eq(nickname))).thenThrow(new ResourceNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/users/nickname/{nickname}", nickname)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserProfile_shouldReturnOkAndUserResponse_whenAdminUpdatesAnyUser() throws Exception {

        // Given
        authenticateAsAdmin();

        UUID targetUserId = UUID.randomUUID();
        String nickname = "testuser";
        String email = "user@test.com";

        UserResponse response = new UserResponse(targetUserId, nickname, email, LocalDateTime.now());
        UserProfileRequest request = new UserProfileRequest(nickname, email);

        when(userService.updateUserProfile(eq(targetUserId), any(UserProfileRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/profile", targetUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(targetUserId.toString()))
                .andExpect(jsonPath("$.nickname").value(nickname));

    }

    @Test
    void updateUserProfile_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
        authenticateAsAdmin();

        UUID targetUserId = UUID.randomUUID();
        String nickname = "testuser";
        String email = "user@test.com";

        UserProfileRequest request = new UserProfileRequest(nickname, email);

        when(userService.updateUserProfile(eq(targetUserId), any(UserProfileRequest.class))).thenThrow(new ResourceNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/profile", targetUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserProfile_shouldReturnConflict_whenNicknameAlreadyExists() throws Exception {

        // Given
        authenticateAsAdmin();

        UUID targetUserId = UUID.randomUUID();
        String nickname = "existingNickname";
        String email = "user@test.com";

        UserProfileRequest request = new UserProfileRequest(nickname, email);

        when(userService.updateUserProfile(eq(targetUserId), any(UserProfileRequest.class))).thenThrow(new DuplicateResourceException("Nickname is already taken"));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/profile", targetUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUserProfile_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {

        // Given
        authenticateAsAdmin();

        UUID targetUserId = UUID.randomUUID();
        String nickname = "testuser";
        String email = "existingEmail@test.com";

        UserProfileRequest request = new UserProfileRequest(nickname, email);

        when(userService.updateUserProfile(eq(targetUserId), any(UserProfileRequest.class))).thenThrow(new DuplicateResourceException("Email is already taken"));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/profile", targetUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateMyProfile_shouldReturnOkAndUserResponse_whenUpdatedCorrectly() throws Exception {

        // Given
        String nickname = "testuser";
        String email = "user@test.com";

        UserResponse response = new UserResponse(mockUserId, nickname, email, LocalDateTime.now());
        UserProfileRequest request = new UserProfileRequest(nickname, email);

        when(userService.updateUserProfile(eq(mockUserId), any(UserProfileRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(mockUserId.toString()))
                .andExpect(jsonPath("$.nickname").value(nickname));

    }

    @Test
    void updateMyProfile_shouldReturnConflict_whenNicknameAlreadyExists() throws Exception {

        // Given
        String nickname = "existingNickname";
        String email = "user@test.com";

        UserProfileRequest request = new UserProfileRequest(nickname, email);

        when(userService.updateUserProfile(eq(mockUserId), any(UserProfileRequest.class))).thenThrow(new DuplicateResourceException("Nickname is already taken"));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateMyProfile_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {

        // Given
        String nickname = "testuser";
        String email = "existingEmail@test.com";

        UserProfileRequest request = new UserProfileRequest(nickname, email);

        when(userService.updateUserProfile(eq(mockUserId), any(UserProfileRequest.class))).thenThrow(new DuplicateResourceException("Email is already taken"));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void changeUserPassword_shouldReturnNoContent_whenAdminUpdatesUserPassword() throws Exception {

        // Given
        authenticateAsAdmin();

        UUID targetUserId = UUID.randomUUID();
        String oldPassword = "Password1234!";
        String newPassword = "Password5678!";

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        doNothing().when(userService).changeUserPassword(eq(mockUserId), any(PasswordChangeRequest.class));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/password", targetUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changeUserPassword_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {

        // Given
        authenticateAsAdmin();

        UUID targetUserId = UUID.randomUUID();
        String oldPassword = "Password1234!";
        String newPassword = "invalidPassword";

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/password", targetUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeUserPassword_shouldReturnBadRequest_whenOldPasswordIsIncorrect() throws Exception {

        // Given
        authenticateAsAdmin();

        UUID targetUserId = UUID.randomUUID();
        String oldPassword = "WrongPass1234!";
        String newPassword = "Password5678!";

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        doThrow(new BadRequestException("Incorrect old password")).when(userService).changeUserPassword(eq(targetUserId), any(PasswordChangeRequest.class));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/password", targetUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeUserPassword_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
        authenticateAsAdmin();

        UUID targetUserId = UUID.randomUUID();
        String oldPassword = "Password1234!";
        String newPassword = "Password5678!";

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        doThrow(new ResourceNotFoundException("User not found")).when(userService).changeUserPassword(eq(targetUserId), any(PasswordChangeRequest.class));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/password", targetUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeMyPassword_shouldReturnNoContent_whenPasswordUpdatedCorrectly() throws Exception {

        // Given
        String oldPassword = "Password1234!";
        String newPassword = "Password5678!";

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        doNothing().when(userService).changeUserPassword(eq(mockUserId), any(PasswordChangeRequest.class));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changeMyPassword_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {

        // Given
        String oldPassword = "Password1234!";
        String newPassword = "invalidPassword";

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        // When & Then
        mockMvc.perform(patch("/api/v1/users/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeMyPassword_shouldReturnBadRequest_whenOldPasswordIsIncorrect() throws Exception {

        // Given
        String oldPassword = "WrongPass1234!";
        String newPassword = "Password5678!";

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        doThrow(new BadRequestException("Incorrect old password")).when(userService).changeUserPassword(eq(mockUserId), any(PasswordChangeRequest.class));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deleteUser_shouldReturnNoContent_whenAdminDeletesAnyUser() throws Exception {

        // Given
        authenticateAsAdmin();
        UUID targetUserId = UUID.randomUUID();

        doNothing().when(userService).deleteUser(eq(targetUserId));

        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", targetUserId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
        authenticateAsAdmin();
        UUID targetUserId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser(eq(targetUserId));

        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", targetUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMyAccount_shouldReturnNoContent_whenDeletedCorrectly() throws Exception {

        // Given
        doNothing().when(userService).deleteUser(eq(mockUserId));

        // When & Then
        mockMvc.perform(delete("/api/v1/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

}