package com.silphengine.infrastructure.external.tcgdex.mappers;

import com.silphengine.domain.entities.Expansion;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexSetDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TcgDexExpansionMapperTest {

    private TcgDexExpansionMapper tcgDexExpansionMapper;

    @BeforeEach
    void setUp() {
        tcgDexExpansionMapper = new TcgDexExpansionMapperImpl();
    }

    @Test
    void toEntity_shouldMapAllFieldsCorrectly() {

        // Given
        TcgDexSetDetailDto.CardCountDto cardCount = new TcgDexSetDetailDto.CardCountDto(100, 100, 0, 0, 0, 0);
        TcgDexSetDetailDto.SerieDto serie = new TcgDexSetDetailDto.SerieDto("swsh", "Sword & Shield");
        
        TcgDexSetDetailDto dto = new TcgDexSetDetailDto(
                "swsh1", "Sword & Shield Base Set", "https://logo.url", "https://symbol.url",
                "2020-02-07", serie, null, null, null, cardCount, List.of()
        );

        // When
        Expansion result = tcgDexExpansionMapper.toEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals("swsh1", result.getExternalId());
        assertEquals("Sword & Shield Base Set", result.getName());
        assertEquals("https://logo.url", result.getLogoUrl());
        assertEquals(100, result.getTotalCards());
        assertEquals("Sword & Shield", result.getSerieName());
        assertEquals(LocalDate.parse("2020-02-07"), result.getReleaseDate());
    }

    @Test
    void toEntity_shouldHandleNullsSafely() {

        // Given
        TcgDexSetDetailDto dto = new TcgDexSetDetailDto(
                "swsh1", "Sword & Shield", null, null,
                null, null, null, null, null, null, null
        );

        // When
        Expansion result = tcgDexExpansionMapper.toEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals("swsh1", result.getExternalId());
        assertEquals("Sword & Shield", result.getName());
        assertNull(result.getLogoUrl());
        assertEquals(0, result.getTotalCards()); // default int value
        assertNull(result.getSerieName());
        assertNull(result.getReleaseDate());
    }

    @Test
    void updateFromDetailDto_shouldUpdateFieldsCorrectly() {

        // Given
        Expansion existingExpansion = Expansion.builder()
                .externalId("swsh1")
                .name("Old Name")
                .serieName("Old Serie")
                .releaseDate(LocalDate.parse("2000-01-01"))
                .totalCards(10)
                .logoUrl("oldLogo")
                .build();

        TcgDexSetDetailDto.CardCountDto cardCount = new TcgDexSetDetailDto.CardCountDto(150, 100, 0, 0, 0, 0);
        TcgDexSetDetailDto.SerieDto serie = new TcgDexSetDetailDto.SerieDto("swsh", "New Serie");
        TcgDexSetDetailDto dto = new TcgDexSetDetailDto(
                "swsh1", "New Name", "newLogo", null,
                "2023-05-15", serie, null, null, null, cardCount, null
        );

        // When
        tcgDexExpansionMapper.updateFromDetailDto(dto, existingExpansion);

        // Then
        assertEquals("swsh1", existingExpansion.getExternalId()); // Must not change
        assertEquals("New Name", existingExpansion.getName());
        assertEquals("newLogo", existingExpansion.getLogoUrl());
        assertEquals(150, existingExpansion.getTotalCards());
        assertEquals("New Serie", existingExpansion.getSerieName());
        assertEquals(LocalDate.parse("2023-05-15"), existingExpansion.getReleaseDate());
    }

    @Test
    void updateFromDetailDto_shouldNotUpdate_whenDtoIsNull() {

        // Given
        Expansion existingExpansion = Expansion.builder()
                .name("Base Set")
                .build();

        // When
        tcgDexExpansionMapper.updateFromDetailDto(null, existingExpansion);

        // Then
        assertEquals("Base Set", existingExpansion.getName());
    }

    @Test
    void updateFromDetailDto_shouldHandleInvalidDateFormat() {

        // Given
        Expansion existingExpansion = Expansion.builder()
                .releaseDate(LocalDate.parse("2020-01-01"))
                .build();

        TcgDexSetDetailDto dto = new TcgDexSetDetailDto(
                "id", "Name", null, null,
                "invalid-date-format", null, null, null, null, null, null
        );

        // When
        tcgDexExpansionMapper.updateFromDetailDto(dto, existingExpansion);

        // Then
        // As per the mapper's try-catch block, if parsing fails, the local variable 'parsedDate' is null,
        // and updateDetails is called with null for releaseDate, so it should be null.
        assertNull(existingExpansion.getReleaseDate());
    }
}
