package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record DeckRequest(
        @NotNull(message = "User id is mandatory")
        UUID userId,

        @NotBlank(message = "Name is mandatory")
        String name,

        @NotEmpty(message = "Deck must contain cards")
        List<DeckCardRequest> cards
) {}
