package com.silphengine.infrastructure.external.tcgdex.mappers;

import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.BadRequestException;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexCardDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TcgDexCardMapperTest {

    private TcgDexCardMapper tcgDexCardMapper;

    @BeforeEach
    void setUp() {
        tcgDexCardMapper = new TcgDexCardMapperImpl();
    }

    @Test
    void toEntity_shouldMapAllFieldsCorrectly() {

        // Given
        Expansion mockExpansion = Mockito.mock(Expansion.class);
        
        TcgDexCardDetailDto dto = new TcgDexCardDetailDto(
                "base1-1", "1", "Alakazam", "https://image.url", "Pokemon",
                "Ken Sugimori", "Rare", null, null, null, null,
                100, List.of("Psychic", "Colorless"), null, null, null, null, null,
                3, "F", null, null, null
        );

        // When
        Card result = tcgDexCardMapper.toEntity(dto, mockExpansion);

        // Then
        assertNotNull(result);
        assertEquals("base1-1", result.getExternalId());
        assertEquals("Alakazam", result.getName());
        assertEquals("https://image.url", result.getImageUrl());
        assertEquals("Rare", result.getRarity());
        assertEquals("F", result.getRegulationMark());
        assertEquals(mockExpansion, result.getExpansion());
        assertEquals(CardCategory.POKEMON, result.getCardCategory());
        assertNotNull(result.getTypes());
        assertEquals(2, result.getTypes().size());
        assertTrue(result.getTypes().contains(CardType.PSYCHIC));
        assertTrue(result.getTypes().contains(CardType.COLORLESS));
    }

    @Test
    void updateFromDetailDto_shouldUpdateFieldsCorrectly() {

        // Given
        Card existingCard = Card.builder()
                .externalId("base1-1")
                .name("Old Name")
                .rarity("Common")
                .imageUrl("oldUrl")
                .cardCategory(CardCategory.ENERGY)
                .build();

        TcgDexCardDetailDto dto = new TcgDexCardDetailDto(
                "base1-1", "1", "New Name", "newUrl", "Trainer",
                null, "Uncommon", null, null, null, null,
                null, List.of("Fire"), null, null, null, null, null,
                null, "G", null, null, null
        );

        // When
        tcgDexCardMapper.updateFromDetailDto(dto, existingCard);

        // Then
        assertEquals("base1-1", existingCard.getExternalId());
        assertEquals("New Name", existingCard.getName());
        assertEquals("newUrl", existingCard.getImageUrl());
        assertEquals("Uncommon", existingCard.getRarity());
        assertEquals("G", existingCard.getRegulationMark());
        assertEquals(CardCategory.TRAINER, existingCard.getCardCategory());
        assertNotNull(existingCard.getTypes());
        assertEquals(1, existingCard.getTypes().size());
        assertTrue(existingCard.getTypes().contains(CardType.FIRE));
    }

    @Test
    void updateFromDetailDto_shouldNotUpdate_whenDtoIsNull() {

        // Given
        Card existingCard = Card.builder()
                .name("Pikachu")
                .build();

        // When
        tcgDexCardMapper.updateFromDetailDto(null, existingCard);

        // Then
        assertEquals("Pikachu", existingCard.getName());
    }

    @Test
    void mapStringToCardCategory_shouldReturnCorrectEnum() {

        assertEquals(CardCategory.POKEMON, tcgDexCardMapper.mapStringToCardCategory("Pokemon"));
        assertEquals(CardCategory.TRAINER, tcgDexCardMapper.mapStringToCardCategory("trainer"));
        assertEquals(CardCategory.ENERGY, tcgDexCardMapper.mapStringToCardCategory(" ENERGY "));
    }

    @Test
    void mapStringToCardCategory_shouldThrowBadRequest_whenInvalidOrNull() {

        assertThrows(BadRequestException.class, () -> tcgDexCardMapper.mapStringToCardCategory(null));
        assertThrows(BadRequestException.class, () -> tcgDexCardMapper.mapStringToCardCategory(""));
        assertThrows(BadRequestException.class, () -> tcgDexCardMapper.mapStringToCardCategory("UnknownCategory"));
    }

    @Test
    void mapStringsToTypes_shouldReturnCorrectEnums() {

        List<String> input = List.of("Fire", " water ", "gRaSS");
        
        List<CardType> result = tcgDexCardMapper.mapStringsToTypes(input);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(CardType.FIRE));
        assertTrue(result.contains(CardType.WATER));
        assertTrue(result.contains(CardType.GRASS));
    }

    @Test
    void mapStringsToTypes_shouldThrowBadRequest_whenInvalidType() {

        List<String> input = List.of("Fire", "InvalidType", "Darkness");
        
        assertThrows(BadRequestException.class, () -> tcgDexCardMapper.mapStringsToTypes(input));
    }

    @Test
    void mapStringsToTypes_shouldReturnEmptyList_whenNullOrEmpty() {

        assertTrue(tcgDexCardMapper.mapStringsToTypes(null).isEmpty());
        assertTrue(tcgDexCardMapper.mapStringsToTypes(List.of()).isEmpty());
    }
}
