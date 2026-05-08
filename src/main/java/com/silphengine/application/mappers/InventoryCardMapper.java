package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.InventoryCardRequest;
import com.silphengine.domain.dto.requests.UpdateInventoryCardRequest;
import com.silphengine.domain.dto.responses.InventoryCardResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.InventoryCard;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.CardCondition;
import com.silphengine.domain.enums.CardFinish;
import com.silphengine.domain.exceptions.BadRequestException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface InventoryCardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "card", source = "card")
    @Mapping(target = "cardCondition", source = "request.cardCondition", qualifiedByName = "stringToCondition")
    @Mapping(target = "cardFinish", source = "request.cardFinish", qualifiedByName = "stringToFinish")
    @Mapping(target = "quantity", source = "request.quantity")
    InventoryCard toEntity(InventoryCardRequest request, User owner, Card card);

    @Mapping(target = "card", source = "entity.card")
    InventoryCardResponse toResponse(InventoryCard entity);

    default void updateEntityFromRequest(InventoryCard card, UpdateInventoryCardRequest request) {

        if (request == null || card == null) {
            return;
        }

        card.changeQuantity(request.quantity());
    }

    @Named("stringToCondition")
    default CardCondition mapStringToCardCondition(String cardConditionString) {

        if (cardConditionString == null || cardConditionString.isBlank()) {
            throw new BadRequestException("Card condition cannot be null or empty");
        }
        try{
            return CardCondition.valueOf(cardConditionString.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Condition not recognized: " + cardConditionString);
        }
    }

    @Named("stringToFinish")
    default CardFinish mapStringToCardFInish(String cardFinishString) {

        if (cardFinishString == null || cardFinishString.isBlank()) {
            throw new BadRequestException("Card finish cannot be null or empty");
        }
        try{
            return CardFinish.valueOf(cardFinishString.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Finish not recognized: " + cardFinishString);
        }
    }
}
