package com.silphengine.infrastructure.web.controllers.api;

import com.silphengine.domain.dto.requests.PasswordChangeRequest;
import com.silphengine.domain.dto.requests.UserProfileRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.UserResponse;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserById(user.getId()));
    }

    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<UserResponse> getUserByNickname(@PathVariable String nickname) {
        return ResponseEntity.ok(userService.getUserByNickname(nickname));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/profile")
    public ResponseEntity<UserResponse> updateUserProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UserProfileRequest request) {

        return ResponseEntity.ok(userService.updateUserProfile(id, request));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal User user, 
            @Valid @RequestBody UserProfileRequest request) {

        return ResponseEntity.ok(userService.updateUserProfile(user.getId(), request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changeUserPassword(
            @PathVariable UUID id,
            @Valid @RequestBody PasswordChangeRequest request) {

        userService.changeUserPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            @AuthenticationPrincipal User user, 
            @Valid @RequestBody PasswordChangeRequest request) {

        userService.changeUserPassword(user.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal User user) {

        userService.deleteUser(user.getId());
        return ResponseEntity.noContent().build();
    }
}
