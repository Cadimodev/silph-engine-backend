package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.CardRequest;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardMapperTest {

    private CardMapper cardMapper;

    @BeforeEach
    void setUp() {
        cardMapper = new CardMapperImpl();
    }

    @Test
    void mapStringToCardCategory_shouldReturnEnum_whenStringIsValid() {

        // Given
        String validCategory = " pokemon ";

        // When
        CardCategory result = cardMapper.mapStringToCardCategory(validCategory);

        // Then
        assertEquals(CardCategory.POKEMON, result);
    }

    @Test
    void mapStringToCardCategory_shouldThrowBadRequestException_whenStringIsNullOrBlank() {

        // Given
        String nullCategory = null;
        String blankCategory = "   ";

        // When & Then
        assertThrows(BadRequestException.class, () -> cardMapper.mapStringToCardCategory(nullCategory));
        assertThrows(BadRequestException.class, () -> cardMapper.mapStringToCardCategory(blankCategory));
    }

    @Test
    void mapStringToCardCategory_shouldThrowBadRequestException_whenStringIsInvalid() {

        // Given
        String invalidCategory = "DIGIMON";

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> cardMapper.mapStringToCardCategory(invalidCategory));
        assertTrue(exception.getMessage().contains("Category not recognized"));
    }

    @Test
    void mapStringsToTypes_shouldReturnEnumList_whenListIsValid() {

        // Given
        List<String> validTypes = List.of("water", " FIRE ");

        // When
        List<CardType> result = cardMapper.mapStringsToTypes(validTypes);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(CardType.WATER));
        assertTrue(result.contains(CardType.FIRE));
    }

    @Test
    void mapStringsToTypes_shouldReturnEmptyList_whenListIsNullOrEmpty() {

        // Given
        List<String> nullList = null;
        List<String> emptyList = Collections.emptyList();

        // When
        List<CardType> resultNull = cardMapper.mapStringsToTypes(nullList);
        List<CardType> resultEmpty = cardMapper.mapStringsToTypes(emptyList);

        // Then
        assertTrue(resultNull.isEmpty());
        assertTrue(resultEmpty.isEmpty());
    }

    @Test
    void mapStringsToTypes_shouldThrowBadRequestException_whenAnyTypeIsInvalid() {
        // Given
        List<String> invalidTypes = List.of("Water", "MAGIC");

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> cardMapper.mapStringsToTypes(invalidTypes));
        assertTrue(exception.getMessage().contains("Card type not recognized"));
    }

    @Test
    void updateEntityFromRequest_shouldUpdateCardProperly_whenRequestIsValid() {

        // Given
        Card existingCard = Card.builder()
                .name("OldName")
                .rarity("Common")
                .cardCategory(CardCategory.TRAINER)
                .types(List.of())
                .imageUrl("oldUrl")
                .build();

        CardRequest updateRequest = new CardRequest(
                "ext-id",
                "NewName",
                "exp-id",
                "Rare",
                "Pokemon",
                List.of("Water"),
                "newUrl"
        );

        // When
        cardMapper.updateEntityFromRequest(existingCard, updateRequest);

        // Then
        assertEquals("NewName", existingCard.getName());
        assertEquals("Rare", existingCard.getRarity());
        assertEquals(CardCategory.POKEMON, existingCard.getCardCategory());
        assertEquals(1, existingCard.getTypes().size());
        assertEquals(CardType.WATER, existingCard.getTypes().getFirst());
        assertEquals("newUrl", existingCard.getImageUrl());
    }

    @Test
    void updateEntityFromRequest_shouldDoNothing_whenRequestOrCardIsNull() {

        // Given
        Card existingCard = Card.builder().name("OldName").build();

        // When
        cardMapper.updateEntityFromRequest(existingCard, null);
        cardMapper.updateEntityFromRequest(null, new CardRequest(null, null, null, null, null, null, null));

        // Then
        assertEquals("OldName", existingCard.getName());
    }
}