package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.entities.Expansion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpansionMapperTest {

    private ExpansionMapper expansionMapper;

    @BeforeEach
    void setUp() {

        expansionMapper = new ExpansionMapperImpl();
    }

    @Test
    void updateEntityFromRequest_shouldUpdateExpansionProperly_whenRequestIsValid() {

        // Given
        Expansion existingExpansion = Expansion.builder()
                .externalId("sv02")
                .name("Old Name")
                .serieName("Old Serie")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .totalCards(100)
                .logoUrl("oldLogoUrl")
                .build();

        ExpansionRequest updateRequest = new ExpansionRequest(
                "sv02",
                "Paldea Evolved",
                "Scarlet & Violet",
                LocalDate.of(2023, 6, 9),
                279,
                "newLogoUrl"
        );

        // When
        expansionMapper.updateEntityFromRequest(existingExpansion, updateRequest);

        // Then
        assertEquals("Paldea Evolved", existingExpansion.getName());
        assertEquals("Scarlet & Violet", existingExpansion.getSerieName());
        assertEquals(LocalDate.of(2023, 6, 9), existingExpansion.getReleaseDate());
        assertEquals(279, existingExpansion.getTotalCards());
        assertEquals("newLogoUrl", existingExpansion.getLogoUrl());

        assertEquals("sv02", existingExpansion.getExternalId());
    }

    @Test
    void updateEntityFromRequest_shouldDoNothing_whenRequestOrExpansionIsNull() {

        // Given
        Expansion existingExpansion = Expansion.builder()
                .name("Old Name")
                .build();

        // When & Then
        assertDoesNotThrow(() -> expansionMapper.updateEntityFromRequest(existingExpansion, null));

        assertDoesNotThrow(() -> expansionMapper.updateEntityFromRequest(null,
                new ExpansionRequest("id", "name", "serie", LocalDate.now(), 10, "url")));
        
        assertEquals("Old Name", existingExpansion.getName());
    }
}