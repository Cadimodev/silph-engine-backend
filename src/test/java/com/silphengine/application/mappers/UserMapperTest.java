package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.UserProfileRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {

        userMapper = new UserMapperImpl();
    }

    @Test
    void toEntity_shouldCombineSourcesCorrectly_andPrioritizeEncodedPassword() {

        // Given
        String rawPassword = "RawPassword123!";
        String encodedPassword = "Hashed_ABC_123";

        UserRequest request = new UserRequest(
                "Nova",
                "nova@silph.com",
                rawPassword
        );

        // When
        User result = userMapper.toEntity(request, encodedPassword);

        // Then
        assertNotNull(result);
        assertEquals("Nova", result.getNickname());
        assertEquals("nova@silph.com", result.getEmail());

        assertEquals(encodedPassword, result.getPassword());
        assertNotEquals(rawPassword, result.getPassword());

        assertNull(result.getId());
        assertNull(result.getRole());
        assertNull(result.getCreatedAt());
    }

    @Test
    void updateEntityFromRequest_shouldUpdateProfileProperly_whenRequestIsValid() {

        // Given
        User existingUser = User.builder()
                .nickname("OldTrainer")
                .email("old@silph.com")
                .password("Password123!")
                .build();

        UserProfileRequest updateRequest = new UserProfileRequest(
                "NewTrainer",
                "new@silph.com"
        );

        // When
        userMapper.updateEntityFromRequest(existingUser, updateRequest);

        // Then
        assertEquals("NewTrainer", existingUser.getNickname());
        assertEquals("new@silph.com", existingUser.getEmail());

        assertEquals("Password123!", existingUser.getPassword());
    }

    @Test
    void updateEntityFromRequest_shouldDoNothing_whenRequestOrUserIsNull() {

        // Given
        User existingUser = User.builder()
                .nickname("OldTrainer")
                .email("old@silph.com")
                .build();

        // When & Then
        assertDoesNotThrow(() -> userMapper.updateEntityFromRequest(existingUser, null));

        assertDoesNotThrow(() -> userMapper.updateEntityFromRequest(null,
                new UserProfileRequest("nick", "mail@mail.com")));

        assertEquals("OldTrainer", existingUser.getNickname());
        assertEquals("old@silph.com", existingUser.getEmail());
    }
}