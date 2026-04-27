package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.dto.requests.PasswordChangeRequest;
import com.silphengine.domain.dto.requests.UserProfileRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.UserResponse;
import com.silphengine.domain.exceptions.BadRequestException;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.UserService;
import com.silphengine.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
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
    void createUser_shouldReturnCreatedAndUserResponse_whenCreatedSuccessfully() throws Exception {

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
        String nickname = "existingNickname";
        String email = "user@test.com";
        String password = "Password1234!";

        UserRequest request = new UserRequest(nickname, email, password);

        when(userService.createUser(any(UserRequest.class))).thenThrow(new DuplicateResourceException("Nickname already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {

        // Given
        String nickname = "testuser";
        String email = "existingEmail@test.com";
        String password = "Password1234!";

        UserRequest request = new UserRequest(nickname, email, password);

        when(userService.createUser(any(UserRequest.class))).thenThrow(new DuplicateResourceException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUserById_shouldReturnOkAndUserResponse_whenUserExists() throws Exception {

        // Given
        String nickname = "testuser";
        String email = "user@test.com";
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        UserResponse response = new UserResponse(id, nickname, email, createdAt);

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
        UUID id = UUID.randomUUID();

        when(userService.getUserById(eq(id))).thenThrow(new ResourceNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
    void updateUserProfile_shouldReturnOkAndUserResponse_whenUpdatedCorrectly() throws Exception {

        // Given
        String nickname = "testuser";
        String email = "user@test.com";
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        UserResponse response = new UserResponse(id, nickname, email, createdAt);
        UserProfileRequest request = new UserProfileRequest(nickname, email);

        when(userService.updateUserProfile(eq(id), any(UserProfileRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/profile", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nickname").value(nickname));

    }

    @Test
    void updateUserProfile_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
        String nickname = "testuser";
        String email = "user@test.com";
        UUID id = UUID.randomUUID();

        UserProfileRequest request = new UserProfileRequest(nickname, email);

        when(userService.updateUserProfile(eq(id), any(UserProfileRequest.class))).thenThrow(new ResourceNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/profile", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserProfile_shouldReturnConflict_whenNicknameAlreadyExists() throws Exception {

        // Given
        String nickname = "existingNickname";
        String email = "user@test.com";
        UUID id = UUID.randomUUID();

        UserProfileRequest request = new UserProfileRequest(nickname, email);

        when(userService.updateUserProfile(eq(id), any(UserProfileRequest.class))).thenThrow(new DuplicateResourceException("Nickname is already taken"));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/profile", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUserProfile_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {

        // Given
        String nickname = "testuser";
        String email = "existingEmail@test.com";
        UUID id = UUID.randomUUID();

        UserProfileRequest request = new UserProfileRequest(nickname, email);

        when(userService.updateUserProfile(eq(id), any(UserProfileRequest.class))).thenThrow(new DuplicateResourceException("Email is already taken"));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/profile", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void changeUserPassword_shouldReturnNoContent_whenUpdatedCorrectly() throws Exception {

        // Given
        String oldPassword = "Password1234!";
        String newPassword = "Password5678!";
        UUID id = UUID.randomUUID();

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        doNothing().when(userService).changeUserPassword(eq(id), any(PasswordChangeRequest.class));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/password", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changeUserPassword_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {

        // Given
        String oldPassword = "Password1234!";
        String newPassword = "invalidPassword";
        UUID id = UUID.randomUUID();

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/password", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeUserPassword_shouldReturnBadRequest_whenOldPasswordIsIncorrect() throws Exception {

        // Given
        String oldPassword = "WrongPass1234!";
        String newPassword = "Password5678!";
        UUID id = UUID.randomUUID();

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        doThrow(new BadRequestException("Incorrect old password")).when(userService).changeUserPassword(eq(id), any(PasswordChangeRequest.class));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/password", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeUserPassword_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
        String oldPassword = "Password1234!";
        String newPassword = "Password5678!";
        UUID id = UUID.randomUUID();

        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        doThrow(new ResourceNotFoundException("User not found")).when(userService).changeUserPassword(eq(id), any(PasswordChangeRequest.class));

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/password", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldReturnNoContent_whenDeletedCorrectly() throws Exception {

        // Given
        UUID id = UUID.randomUUID();

        doNothing().when(userService).deleteUser(eq(id));

        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExists() throws Exception {

        // Given
        UUID id = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser(eq(id));

        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
