package com.silphengine.domain.dto.responses;

import java.util.List;
import java.util.UUID;

public record DeckResponse(
        UUID id,
        UUID ownerId,
        String name,
        Boolean isLegal,
        List<DeckCardResponse> cards
) {}
