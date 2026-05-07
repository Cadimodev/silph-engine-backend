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
import com.silphengine.infrastructure.web.config.TestSecurityConfig;
import com.silphengine.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import com.silphengine.security.annotations.WithMockCustomUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;


    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void createUser_shouldReturnCreatedAndUserResponse_whenAdminCreatesUser() throws Exception {

        // Given
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
    @WithMockCustomUser
    void createUser_shouldReturnForbidden_whenHavingUserRole() throws Exception {

        // Given

        UserRequest request = new UserRequest("user", "user@test.com", "Password1234!");

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).createUser(any());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void createUser_shouldReturnConflict_whenNicknameAlreadyExists() throws Exception {

        // Given
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
    @WithMockCustomUser(roles = {"ADMIN"})
    void createUser_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {

        // Given
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
    @WithMockCustomUser(roles = {"ADMIN"})
    void getUserById_shouldReturnOkAndUserResponse_whenAdminRequestsAnyUser() throws Exception {

        // Given
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
    @WithMockCustomUser(roles = {"ADMIN"})
    void getUserById_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
        UUID id = UUID.randomUUID();

        when(userService.getUserById(eq(id))).thenThrow(new ResourceNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void getMyProfile_shouldReturnOkAndUserResponse() throws Exception {

        // Given
        UUID mockUserId = getAuthenticatedUserId();
        UserResponse response = new UserResponse(mockUserId, "testuser", "user@test.com", LocalDateTime.now());
        when(userService.getUserById(eq(mockUserId))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockUserId.toString()));
    }

    @Test
    @WithMockCustomUser
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
    @WithMockCustomUser
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
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateUserProfile_shouldReturnOkAndUserResponse_whenAdminUpdatesAnyUser() throws Exception {

        // Given
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
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateUserProfile_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
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
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateUserProfile_shouldReturnConflict_whenNicknameAlreadyExists() throws Exception {

        // Given
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
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateUserProfile_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {

        // Given
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
    @WithMockCustomUser
    void updateMyProfile_shouldReturnOkAndUserResponse_whenUpdatedCorrectly() throws Exception {

        // Given
        UUID mockUserId = getAuthenticatedUserId();
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
    @WithMockCustomUser
    void updateMyProfile_shouldReturnConflict_whenNicknameAlreadyExists() throws Exception {

        // Given
        UUID mockUserId = getAuthenticatedUserId();
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
    @WithMockCustomUser
    void updateMyProfile_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {

        // Given
        UUID mockUserId = getAuthenticatedUserId();
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
    @WithMockCustomUser(roles = {"ADMIN"})
    void changeUserPassword_shouldReturnNoContent_whenAdminUpdatesUserPassword() throws Exception {

        // Given
        UUID targetUserId = UUID.randomUUID();
        String oldPassword = "Password1234!";
        String newPassword = "Password5678!";

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        doNothing().when(userService).changeUserPassword(eq(targetUserId), any(PasswordChangeRequest.class));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/password", targetUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void changeUserPassword_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {

        // Given
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
    @WithMockCustomUser(roles = {"ADMIN"})
    void changeUserPassword_shouldReturnBadRequest_whenOldPasswordIsIncorrect() throws Exception {

        // Given
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
    @WithMockCustomUser(roles = {"ADMIN"})
    void changeUserPassword_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
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
    @WithMockCustomUser
    void changeMyPassword_shouldReturnNoContent_whenPasswordUpdatedCorrectly() throws Exception {

        // Given
        UUID mockUserId = getAuthenticatedUserId();
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
    @WithMockCustomUser
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
    @WithMockCustomUser
    void changeMyPassword_shouldReturnBadRequest_whenOldPasswordIsIncorrect() throws Exception {

        // Given
        UUID mockUserId = getAuthenticatedUserId();
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
    @WithMockCustomUser(roles = {"ADMIN"})
    void deleteUser_shouldReturnNoContent_whenAdminDeletesAnyUser() throws Exception {

        // Given
        UUID targetUserId = UUID.randomUUID();

        doNothing().when(userService).deleteUser(eq(targetUserId));

        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", targetUserId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
        UUID targetUserId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser(eq(targetUserId));

        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", targetUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void deleteMyAccount_shouldReturnNoContent_whenDeletedCorrectly() throws Exception {

        // Given
        UUID mockUserId = getAuthenticatedUserId();

        doNothing().when(userService).deleteUser(eq(mockUserId));

        // When & Then
        mockMvc.perform(delete("/api/v1/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    private UUID getAuthenticatedUserId() {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Objects.requireNonNull(user);
        return user.getId();
    }

}