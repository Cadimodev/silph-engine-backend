package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.CardRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CardMapper {

    public Card toEntity(CardRequest request, Expansion expansion) {
        return Card.builder()
                .externalId(request.externalId())
                .name(request.name())
                .rarity(request.rarity())
                .cardCategory(mapStringToCardCategory(request.cardCategory()))
                .imageUrl(request.imageUrl())
                .expansion(expansion)
                .types(mapStringsToTypes(request.types()))
                .build();
    }

    public CardResponse toResponse(Card entity) {
        return new CardResponse(
                entity.getExternalId(),
                entity.getName(),
                entity.getRarity(),
                entity.getCardCategory(),
                entity.getTypes(),
                entity.getImageUrl(),
                entity.getExpansion().getExternalId()
        );
    }

    public void updateEntityFromRequest(Card card, CardRequest cardRequest) {
        CardCategory mappedCategory = mapStringToCardCategory(cardRequest.cardCategory());
        List<CardType> mappedTypes = mapStringsToTypes(cardRequest.types());

        card.updateDetails(
                cardRequest.name(),
                cardRequest.rarity(),
                mappedCategory,
                mappedTypes,
                cardRequest.imageUrl()
        );
    }

    private CardCategory mapStringToCardCategory(String cardCategoryString) {

        if (cardCategoryString == null || cardCategoryString.isBlank()) {
            throw new BadRequestException("Card category cannot be null or empty");
        }

        try {
            return CardCategory.valueOf(cardCategoryString.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Category not recognized: " + cardCategoryString);
        }
    }

    private List<CardType> mapStringsToTypes(List<String> typeStrings) {
        if (typeStrings == null || typeStrings.isEmpty()) {
            return new ArrayList<>();
        }

        return typeStrings.stream()
                .map(type -> {
                    try {
                        return CardType.valueOf(type.toUpperCase().trim());
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Card type not recognized: " + type);
                    }
                })
                .collect(Collectors.toList());
    }
}
