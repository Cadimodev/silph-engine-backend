package com.silphengine.application.services;

import com.silphengine.application.mappers.UserMapper;
import com.silphengine.application.mappers.UserMapperImpl;
import com.silphengine.domain.dto.requests.PasswordChangeRequest;
import com.silphengine.domain.dto.requests.UserProfileRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.UserResponse;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.Role;
import com.silphengine.domain.exceptions.BadRequestException;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.UserService;
import com.silphengine.infrastructure.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper;

    private UserService userService;

    private User user;
    private UserRequest userRequest;
    private UserProfileRequest userProfileRequest;
    private String nickname;
    private String email;
    private String nicknameUpdate;
    private String emailUpdate;

    @BeforeEach
    void setUp() {

        userMapper = new UserMapperImpl();
        userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder);

        nickname = "testuser";
        email = "test@example.com";
        nicknameUpdate = "testuserupdate";
        emailUpdate = "testUpdate@example.com";
        String password = "Password123;";
        String fakeEncodedPassword = "EncodedPassword123;";

        userRequest = new UserRequest(nickname, email, password);
        userProfileRequest = new UserProfileRequest(nicknameUpdate, emailUpdate);

        user = User.builder()
                .id(UUID.randomUUID())
                .nickname(userRequest.nickname())
                .email(userRequest.email())
                .password(fakeEncodedPassword)
                .role(Role.USER)
                .build();
    }

    @Test
    void createUser_shouldReturnUserResponse_whenUserDoesNotExists() {

        // Given
        when(userRepository.findByNicknameOrEmail(userRequest.nickname(), userRequest.email())).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(user);

        // When
        UserResponse result = userService.createUser(userRequest);

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getNickname(), result.nickname());

        verify(userRepository, times(1)).findByNicknameOrEmail(userRequest.nickname(), userRequest.email());
        verify(userRepository, times(1)).saveAndFlush(any(User.class));
    }

    @Test
    void createUser_shouldThrowDuplicateResourceException_whenNicknameAlreadyExists() {

        User existingUserWithSameNickname = User.builder()
                .id(UUID.randomUUID())
                .nickname(nickname)
                .email("another-email@example.com")
                .password("another-pass")
                .build();

        // Given
        when(userRepository.findByNicknameOrEmail(userRequest.nickname(), userRequest.email()))
                .thenReturn(Optional.of(existingUserWithSameNickname));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(userRequest));

        assertEquals("User with nickname: " + userRequest.nickname() + " already exists", exception.getMessage());

        verify(userRepository, times(1)).findByNicknameOrEmail(userRequest.nickname(), userRequest.email());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void createUser_shouldThrowDuplicateResourceException_whenEmailAlreadyExists() {

        User existingUserWithSameEmail = User.builder()
                .id(UUID.randomUUID())
                .nickname("another-user")
                .email(email)
                .password("another-pass")
                .build();

        // Given
        when(userRepository.findByNicknameOrEmail(userRequest.nickname(), userRequest.email()))
                .thenReturn(Optional.of(existingUserWithSameEmail));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(userRequest));

        assertEquals("User with email: " + userRequest.email() + " already exists", exception.getMessage());

        verify(userRepository, times(1)).findByNicknameOrEmail(userRequest.nickname(), userRequest.email());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_shouldReturnUserResponse_whenUserExists() {

        // Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        UserResponse result = userService.getUserById(user.getId());

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getNickname(), result.nickname());

        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    void getUserById_shouldThrowResourceNotFoundException_whenUserDoesNotExists() {

        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(nonExistentId));

        assertEquals("User with ID: " + nonExistentId + " not found", exception.getMessage());

        verify(userRepository, times(1)).findById(nonExistentId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserByNickname_shouldReturnUserResponse_whenUserExists() {

        // Given
        when(userRepository.findByNickname(userRequest.nickname())).thenReturn(Optional.of(user));

        // When
        UserResponse result = userService.getUserByNickname(userRequest.nickname());

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getNickname(), result.nickname());

        verify(userRepository, times(1)).findByNickname(userRequest.nickname());
    }

    @Test
    void getUserByNickname_shouldThrowResourceNotFoundException_whenUserDoesNotExists() {

        // Given
        String nonExistentNickname = "nonexistent";
        when(userRepository.findByNickname(nonExistentNickname)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByNickname(nonExistentNickname));

        assertEquals("User with nickname: " + nonExistentNickname + " not found", exception.getMessage());

        verify(userRepository, times(1)).findByNickname(nonExistentNickname);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserProfile_shouldReturnUserResponse_whenEverythingGoesSuccessfully() {

        // Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByNickname(userProfileRequest.nickname())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userProfileRequest.email())).thenReturn(Optional.empty());

        // When
        UserResponse result = userService.updateUserProfile(user.getId(), userProfileRequest);

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(userProfileRequest.nickname(), result.nickname());
        assertEquals(userProfileRequest.email(), result.email());

        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).findByNickname(userProfileRequest.nickname());
        verify(userRepository, times(1)).findByEmail(userProfileRequest.email());
    }

    @Test
    void updateUserProfile_shouldThrowResourceNotFoundException_whenUserDoesNotExists() {

        // Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUserProfile(user.getId(), userProfileRequest));

        assertEquals("User with ID: " + user.getId() + " not found", exception.getMessage());
        verify(userRepository, times(1)).findById(user.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserProfile_shouldThrowDuplicateResourceException_whenNicknameAlreadyExists() {

        // Given
        User existingUserWithSameNickname = User.builder()
                .id(UUID.randomUUID())
                .nickname(nicknameUpdate)
                .email("another-email@example.com")
                .password("another-pass")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByNickname(userProfileRequest.nickname())).thenReturn(Optional.of(existingUserWithSameNickname));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> userService.updateUserProfile(user.getId(), userProfileRequest));

        assertEquals("Nickname " + userProfileRequest.nickname() + " is already taken.", exception.getMessage());
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).findByNickname(userProfileRequest.nickname());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserProfile_shouldThrowDuplicateResourceException_whenEmailAlreadyExists() {

        // Given
        User existingUserWithSameEmail = User.builder()
                .id(UUID.randomUUID())
                .nickname("another-user")
                .email(emailUpdate)
                .password("another-pass")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByNickname(userProfileRequest.nickname())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userProfileRequest.email())).thenReturn(Optional.of(existingUserWithSameEmail));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> userService.updateUserProfile(user.getId(), userProfileRequest));

        assertEquals("Email " + userProfileRequest.email() + " is already taken.", exception.getMessage());
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).findByNickname(userProfileRequest.nickname());
        verify(userRepository, times(1)).findByEmail(userProfileRequest.email());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void changeUserPassword_shouldDoNothing_whenEverythingGoesSuccessfully() {
        // Given
        String oldPassword = "Password123;";
        String newPassword = "newPassword456";
        String newEncodedPassword = "newEncodedPassword456";
        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        String originalEncodedPassword = user.getPassword();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, originalEncodedPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(newEncodedPassword);

        // When
        userService.changeUserPassword(user.getId(), request);

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(passwordEncoder, times(1)).matches(oldPassword, originalEncodedPassword);
        verify(passwordEncoder, times(1)).encode(newPassword);
        assertEquals(newEncodedPassword, user.getPassword());
    }

    @Test
    void changeUserPassword_shouldThrowBadRequestException_whenOldPasswordIsIncorrect() {
        // Given
        String oldPassword = "wrongOldPassword";
        String newPassword = "newPassword456";
        PasswordChangeRequest request = new PasswordChangeRequest(oldPassword, newPassword);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.changeUserPassword(user.getId(), request));

        assertEquals("Incorrect old password.", exception.getMessage());
        verify(userRepository, times(1)).findById(user.getId());
        verify(passwordEncoder, times(1)).matches(oldPassword, user.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deleteUser_shouldDoNothing_whenUserExists() {
        // Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // When
        userService.deleteUser(user.getId());

        // Then
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(nonExistentId));

        assertEquals("User with ID: " + nonExistentId + " not found", exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistentId);
        verify(userRepository, never()).delete(any(User.class));
    }
}
