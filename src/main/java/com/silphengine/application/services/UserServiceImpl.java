package com.silphengine.application.services;

import com.silphengine.application.mappers.UserMapper;
import com.silphengine.domain.dto.requests.PasswordChangeRequest;
import com.silphengine.domain.dto.requests.UserProfileRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.UserResponse;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.exceptions.BadRequestException;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.UserService;
import com.silphengine.infrastructure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {

        userRepository.findByNicknameOrEmail(userRequest.nickname(), userRequest.email()).ifPresent(
                existingUser -> {
                    if (existingUser.getNickname().equals(userRequest.nickname())) {
                        throw new DuplicateResourceException("User with nickname: " + userRequest.nickname() + " already exists");
                    } else {
                        throw new DuplicateResourceException("User with email: " + userRequest.email() + " already exists");
                    }
                }
        );

        String encodedPassword = passwordEncoder.encode(userRequest.password());
        User user = userMapper.toEntity(userRequest, encodedPassword);
        user.assignDefaultRole();

        // saveAndFlush to force immediate synchronization with the database,
        // which populates the @CreationTimestamp field.
        User savedUser = userRepository.saveAndFlush(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        return userMapper.toResponse(findUserById(id));
    }

    @Override
    public UserResponse getUserByNickname(String nickname) {
        return userMapper.toResponse(userRepository.findByNickname(nickname).orElseThrow(() ->
                new ResourceNotFoundException("User with nickname: " + nickname + " not found")));
    }

    @Override
    @Transactional
    public UserResponse updateUserProfile(UUID id, UserProfileRequest userProfileRequest) {
        User user = findUserById(id);

        // Check if the new nickname is already taken by another user
        if (!user.getNickname().equals(userProfileRequest.nickname())) {
            userRepository.findByNickname(userProfileRequest.nickname()).ifPresent(existingUser -> {
                if (!Objects.equals(existingUser.getId(), id)) {
                    throw new DuplicateResourceException("Nickname " + userProfileRequest.nickname() + " is already taken.");
                }
            });
        }

        // Check if the new email is already taken by another user
        if (!user.getEmail().equals(userProfileRequest.email())) {
            userRepository.findByEmail(userProfileRequest.email()).ifPresent(existingUser -> {
                if (!Objects.equals(existingUser.getId(), id)) {
                    throw new DuplicateResourceException("Email " + userProfileRequest.email() + " is already taken.");
                }
            });
        }

        userMapper.updateEntityFromRequest(user, userProfileRequest);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void changeUserPassword(UUID id, PasswordChangeRequest passwordChangeRequest) {
        User user = findUserById(id);

        // Verify the old password
        if (!passwordEncoder.matches(passwordChangeRequest.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect old password.");
        }

        String newEncodedPassword = passwordEncoder.encode(passwordChangeRequest.newPassword());
        user.changePassword(newEncodedPassword);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        userRepository.delete(findUserById(id));
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("User with ID: " + id + " not found"));
    }
}
