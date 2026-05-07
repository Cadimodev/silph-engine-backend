package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.DeckRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.dto.responses.DeckCardResponse;
import com.silphengine.domain.dto.responses.DeckResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Deck;
import com.silphengine.domain.entities.DeckCard;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeckMapperTest {

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private DeckMapperImpl deckMapper;

    private User owner;
    private DeckRequest deckRequest;
    private List<DeckCard> deckCards;
    private Card card;
    private DeckCard deckCard;

    @BeforeEach
    void setUp() {

        owner = User.builder()
                .id(UUID.randomUUID())
                .nickname("Ash")
                .build();

        deckRequest = new DeckRequest("My Awesome Deck", List.of());

        card = Card.builder()
                .id(UUID.randomUUID())
                .externalId("sv02-203")
                .name("Magikarp")
                .rarity("Illustration rare")
                .cardCategory(CardCategory.POKEMON)
                .types(List.of(CardType.WATER))
                .imageUrl("https://assets.tcgdex.net/en/sv/sv02/203")
                .regulationMark("G")
                .build();

        deckCard = DeckCard.builder()
                .card(card)
                .quantity(4)
                .build();

        deckCards = new ArrayList<>(List.of(deckCard));
    }

    @Test
    void toEntity_shouldMapDeckRequestToDeckAndLinkCards() {

        // When
        Deck result = deckMapper.toEntity(deckRequest, owner, deckCards);

        // Then
        assertNotNull(result);
        assertEquals(deckRequest.name(), result.getName());
        assertEquals(owner, result.getOwner());
        assertFalse(result.getIsLegal());

        assertEquals(1, result.getCards().size());
        assertEquals("Magikarp", result.getCards().getFirst().getCard().getName());
        assertEquals(result, result.getCards().getFirst().getDeck());
    }

    @Test
    void toEntity_shouldReturnNull_whenAllInputsAreNull() {
        assertNull(deckMapper.toEntity(null, null, null));
    }

    @Test
    void toResponse_shouldMapDeckToDeckResponse() {

        // Given
        Deck deck = Deck.builder()
                .id(UUID.randomUUID())
                .name("WATER DECK!")
                .owner(owner)
                .isLegal(true)
                .cards(deckCards)
                .build();

        CardResponse mockCardResponse = new CardResponse(
                card.getExternalId(),
                card.getName(),
                card.getRarity(),
                card.getCardCategory(),
                card.getTypes(),
                card.getImageUrl(),
                "sv02",
                card.getRegulationMark());

        when(cardMapper.toResponse(card)).thenReturn(mockCardResponse);

        // When
        DeckResponse result = deckMapper.toResponse(deck);

        // Then
        assertNotNull(result);
        assertEquals(deck.getId(), result.id());
        assertEquals(deck.getName(), result.name());
        assertTrue(result.isLegal());
        assertEquals(owner.getId(), result.ownerId());

        assertEquals(1, result.cards().size());
        DeckCardResponse dcr = result.cards().getFirst();
        assertEquals(4, dcr.quantity());
        assertEquals("Magikarp", dcr.card().name());
    }

    @Test
    void toResponse_shouldReturnNull_whenDeckIsNull() {
        assertNull(deckMapper.toResponse(null));
    }

    @Test
    void updateEntityFromRequest_shouldDoNothing_whenRequestOrDeckIsNull() {

        // Given
        Deck existingDeck = Deck.builder()
                .name("Old Name")
                .build();

        // When & Then
        assertDoesNotThrow(() -> deckMapper.updateEntityFromRequest(existingDeck, null, List.of()));
        assertDoesNotThrow(() -> deckMapper.updateEntityFromRequest(null, deckRequest, List.of()));

        assertEquals("Old Name", existingDeck.getName());
    }

    @Test
    void updateEntityFromRequest_shouldUpdateDeckDetailsAndReplaceCards() {

        // Given
        Deck existingDeck = Deck.builder()
                .name("Old Name")
                .isLegal(true)
                .cards(new ArrayList<>(List.of(DeckCard.builder().quantity(1).build()))) // Carta vieja
                .build();

        // When
        deckMapper.updateEntityFromRequest(existingDeck, deckRequest, deckCards);

        // Then
        assertEquals("My Awesome Deck", existingDeck.getName());
        assertTrue(existingDeck.getIsLegal());
        
        assertEquals(1, existingDeck.getCards().size());
        assertEquals("Magikarp", existingDeck.getCards().getFirst().getCard().getName());
        assertEquals(4, existingDeck.getCards().getFirst().getQuantity());
        assertEquals(existingDeck, existingDeck.getCards().getFirst().getDeck());
    }
}
