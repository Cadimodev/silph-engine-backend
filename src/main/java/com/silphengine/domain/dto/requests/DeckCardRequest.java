package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record DeckCardRequest(
        @NotNull(message = "Card id is mandatory")
        UUID cardId,

        @NotNull(message = "Quantity is mandatory")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity
) {}
