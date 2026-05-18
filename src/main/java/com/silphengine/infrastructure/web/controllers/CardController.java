package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.dto.requests.CardRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.exceptions.BadRequestException;
import com.silphengine.domain.services.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest request) {

        CardResponse response = cardService.createCard(request);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<CardResponse> getCardByExternalId(@PathVariable String externalId) {
        return ResponseEntity.ok(cardService.getByExternalId(externalId));
    }

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getCards(
            @RequestParam(required = false) String expansion,
            Pageable pageable) {

        if (expansion != null) {
            return ResponseEntity.ok(cardService.getByExternalExpansionId(expansion, pageable));
        }

        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{externalId}")
    public ResponseEntity<CardResponse> updateCardByExternalId(
            @PathVariable String externalId,
            @Valid @RequestBody CardRequest request) {

        if (!externalId.equals(request.externalId())) {
            throw new BadRequestException("The ID in the URL (" + externalId +
                    ") does not match the ID in the body (" + request.externalId() + ")");
        }

       return ResponseEntity.ok(cardService.updateByExternalId(externalId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{externalId}")
    public ResponseEntity<Void> deleteCardByExternalId(@PathVariable String externalId) {

        cardService.deleteByExternalId(externalId);
        return ResponseEntity.noContent().build();
    }

}
