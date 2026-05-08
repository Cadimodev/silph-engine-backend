package com.silphengine.domain.dto.responses;

import com.silphengine.domain.enums.CardCondition;
import com.silphengine.domain.enums.CardFinish;

import java.util.UUID;

public record InventoryCardResponse(
        UUID id,
        CardResponse card,
        Integer quantity,
        CardCondition cardCondition,
        CardFinish cardFinish

) {}
