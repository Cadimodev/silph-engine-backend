package com.silphengine.domain.dto.responses;

public record DeckCardResponse(
        CardResponse card,
        Integer quantity
) {}
