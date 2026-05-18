package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.dto.requests.InventoryCardRequest;
import com.silphengine.domain.dto.requests.UpdateInventoryCardRequest;
import com.silphengine.domain.dto.responses.InventoryCardResponse;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.services.InventoryCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collection")
@RequiredArgsConstructor
public class InventoryCardController {

    private final InventoryCardService inventoryCardService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<InventoryCardResponse> createInventoryCard(
            @Valid @RequestBody InventoryCardRequest request,
            @AuthenticationPrincipal User user) {

        InventoryCardResponse response = inventoryCardService.createInventoryCard(request, user.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<Page<InventoryCardResponse>> getCollection(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        
        return ResponseEntity.ok(inventoryCardService.getCollection(user.getId(), pageable));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{inventoryCardId}")
    public ResponseEntity<InventoryCardResponse> getInventoryCard(
            @PathVariable UUID inventoryCardId,
            @AuthenticationPrincipal User user) {
        
        return ResponseEntity.ok(inventoryCardService.getInventoryCard(inventoryCardId, user.getId()));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<List<InventoryCardResponse>> getInventoryCardsByCardId(
            @PathVariable UUID cardId,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(inventoryCardService.getInventoryCardsByCardId(cardId, user.getId()));
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{inventoryCardId}")
    public ResponseEntity<InventoryCardResponse> updateInventoryCard(
            @PathVariable UUID inventoryCardId,
            @Valid @RequestBody UpdateInventoryCardRequest request,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(inventoryCardService.updateInventoryCard(inventoryCardId, request, user.getId()));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{inventoryCardId}")
    public ResponseEntity<Void> deleteInventoryCard(
            @PathVariable UUID inventoryCardId,
            @AuthenticationPrincipal User user) {

        inventoryCardService.deleteInventoryCard(inventoryCardId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
