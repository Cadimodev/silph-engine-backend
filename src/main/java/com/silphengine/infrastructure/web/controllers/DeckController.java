package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.dto.requests.DeckRequest;
import com.silphengine.domain.dto.responses.DeckResponse;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.services.DeckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(
            @Valid @RequestBody DeckRequest request,
            @AuthenticationPrincipal User user) {

        // Pass the authenticated user's ID to the service
        DeckResponse response = deckService.createDeck(request, user.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{deckId}")
    public ResponseEntity<DeckResponse> getDeckById(
            @PathVariable UUID deckId,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(deckService.getByIdAndOwnerID(deckId, user.getId()));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public ResponseEntity<List<DeckResponse>> getMyDecks(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String name) {

        UUID ownerId = user.getId();

        if (name != null && !name.isBlank()) {

            return ResponseEntity.ok(deckService.getByOwnerIdAndDeckName(ownerId, name));
        }

        // If name is empty, we return all user's decks
        return ResponseEntity.ok(deckService.getByOwnerId(ownerId));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{deckId}")
    public ResponseEntity<DeckResponse> updateDeck(
            @PathVariable UUID deckId,
            @Valid @RequestBody DeckRequest request,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(deckService.updateDeck(deckId, request, user.getId()));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> deleteDeck(
            @PathVariable UUID deckId,
            @AuthenticationPrincipal User user) {

        deckService.deleteDeck(deckId, user.getId());

        return ResponseEntity.noContent().build();
    }
}
