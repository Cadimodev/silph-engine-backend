package com.silphengine.domain.dto.responses;

import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import java.util.List;
import java.util.UUID;

public record CardResponse(
        UUID id,
        String externalId,
        String name,
        String rarity,
        CardCategory cardCategory,
        List<CardType> types,
        String imageUrl,
        String expansionExternalId,
        String regulationMark
) {}