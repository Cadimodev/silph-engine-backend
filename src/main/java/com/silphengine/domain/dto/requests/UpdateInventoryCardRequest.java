package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateInventoryCardRequest(
        @NotNull(message = "Quantity is mandatory")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity
) {}
