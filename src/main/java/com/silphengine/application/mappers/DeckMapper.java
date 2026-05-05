package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.DeckRequest;
import com.silphengine.domain.dto.responses.DeckCardResponse;
import com.silphengine.domain.dto.responses.DeckResponse;
import com.silphengine.domain.entities.Deck;
import com.silphengine.domain.entities.DeckCard;
import com.silphengine.domain.entities.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {CardMapper.class})
public interface DeckMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "isLegal", ignore = true)
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "cards", ignore = true)
    Deck toEntity(DeckRequest request, User owner, List<DeckCard> deckCards);
    
    @Mapping(target = "ownerId", source="entity.owner.id")
    DeckResponse toResponse(Deck entity);

    DeckCardResponse toDeckCardResponse(DeckCard deckCard);

    @AfterMapping
    default void linkCardsToDeck(@MappingTarget Deck deck, List<DeckCard> deckCards) {

        if (deckCards != null) {
            for (DeckCard card : deckCards) {
                deck.addCard(card);
            }
        }
    }

    default void updateEntityFromRequest(@MappingTarget Deck deck, DeckRequest request, List<DeckCard> newCards) {
        if (deck == null || request == null) {
            return;
        }

        deck.updateDetails(request.name(), deck.getIsLegal());

        deck.getCards().clear();

        if (newCards != null) {
            for (DeckCard newCard : newCards) {
                deck.addCard(newCard);
            }
        }
    }
}
