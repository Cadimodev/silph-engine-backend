package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.CardRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.BadRequestException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface CardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", source = "request.externalId")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "expansion", source = "expansion")
    @Mapping(target = "cardCategory", source = "request.cardCategory", qualifiedByName = "stringToCategory")
    @Mapping(target = "types", source = "request.types", qualifiedByName = "stringsToTypes")
    Card toEntity(CardRequest request, Expansion expansion);

    @Mapping(target = "expansionExternalId", source = "entity.expansion.externalId")
    CardResponse toResponse(Card entity);

    default void updateEntityFromRequest(Card card, CardRequest cardRequest) {

        if (cardRequest == null || card == null) {
            return;
        }

        card.updateDetails(
                cardRequest.name(),
                cardRequest.rarity(),
                mapStringToCardCategory(cardRequest.cardCategory()),
                mapStringsToTypes(cardRequest.types()),
                cardRequest.imageUrl()
        );
    }

    @Named("stringToCategory")
    default CardCategory mapStringToCardCategory(String cardCategoryString) {

        if (cardCategoryString == null || cardCategoryString.isBlank()) {
            throw new BadRequestException("Card category cannot be null or empty");
        }
        try {
            return CardCategory.valueOf(cardCategoryString.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Category not recognized: " + cardCategoryString);
        }
    }

    @Named("stringsToTypes")
    default List<CardType> mapStringsToTypes(List<String> typeStrings) {

        if (typeStrings == null || typeStrings.isEmpty()) {
            return new ArrayList<>();
        }
        return typeStrings.stream()
                .map(type -> {
                    try {
                        return CardType.valueOf(type.toUpperCase().trim());
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Card type not recognized: " + type);
                    }
                })
                .collect(Collectors.toList());
    }
}
