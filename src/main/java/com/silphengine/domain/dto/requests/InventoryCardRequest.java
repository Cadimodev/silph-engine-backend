package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record InventoryCardRequest(

        @NotNull(message = "CardId is mandatory")
        UUID cardId,

        @NotNull(message = "Quantity is mandatory")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity,

        @NotBlank(message = "CardCondition is mandatory")
        String cardCondition,

        @NotBlank(message = "CardFinish is mandatory")
        String cardFinish
) {}
