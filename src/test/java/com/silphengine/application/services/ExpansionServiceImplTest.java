package com.silphengine.application.services;

import com.silphengine.application.mappers.ExpansionMapper;
import com.silphengine.application.mappers.ExpansionMapperImpl;
import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.dto.responses.ExpansionResponse;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.ExpansionService;
import com.silphengine.infrastructure.repositories.ExpansionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpansionServiceImplTest {

    @Mock
    private ExpansionRepository expansionRepository;

    private ExpansionService expansionService;

    private ExpansionRequest expansionRequest;
    private Expansion expansion;
    private String externalId;

    @BeforeEach
    void setUp() {
        ExpansionMapper expansionMapper = new ExpansionMapperImpl();
        expansionService = new ExpansionServiceImpl(expansionRepository, expansionMapper);

        externalId = "sv02";
        String name = "Paldea Evolved";
        String serieName = "Scarlet & Violet";
        LocalDate releaseDate = LocalDate.of(2023, 6, 9);
        int totalCards = 279;
        String logoUrl = "https://assets.tcgdex.net/en/sv/sv02/logo";

        expansionRequest = new ExpansionRequest(externalId, name, serieName, releaseDate, totalCards, logoUrl);
        expansion = expansionMapper.toEntity(expansionRequest);
    }

    @Test
    void createExpansion_shouldReturnExpansionResponse_whenExpansionIsCreatedSuccessfully() {
        // Given
        when(expansionRepository.findByExternalId(externalId)).thenReturn(Optional.empty());
        when(expansionRepository.save(any(Expansion.class))).thenReturn(expansion);

        // When
        ExpansionResponse result = expansionService.createExpansion(expansionRequest);

        // Then
        assertNotNull(result);
        assertEquals(expansion.getExternalId(), result.externalId());
        assertEquals(expansion.getName(), result.name());
        
        verify(expansionRepository, times(1)).findByExternalId(externalId);
        verify(expansionRepository, times(1)).save(any(Expansion.class));
    }

    @Test
    void createExpansion_shouldThrowDuplicateResourceException_whenExpansionAlreadyExists() {
        // Given
        when(expansionRepository.findByExternalId(externalId)).thenReturn(Optional.of(expansion));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
                expansionService.createExpansion(expansionRequest));
        assertEquals("Expansion already exists with ID: " + externalId, exception.getMessage());
        
        verify(expansionRepository, times(1)).findByExternalId(externalId);
        verifyNoMoreInteractions(expansionRepository);
    }

    @Test
    void getByExternalId_shouldReturnExpansionResponse_whenExpansionExists() {
        // Given
        when(expansionRepository.findByExternalId(externalId)).thenReturn(Optional.of(expansion));

        // When
        ExpansionResponse result = expansionService.getByExternalId(externalId);

        // Then
        assertNotNull(result);
        assertEquals(expansion.getExternalId(), result.externalId());
        assertEquals(expansion.getName(), result.name());

        verify(expansionRepository, times(1)).findByExternalId(externalId);
    }

    @Test
    void getByExternalId_shouldThrowResourceNotFoundException_whenExpansionDoesNotExist() {
        // Given
        when(expansionRepository.findByExternalId(externalId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                expansionService.getByExternalId(externalId));
        assertEquals("Expansion with ID " + externalId + " not found", exception.getMessage());
        verify(expansionRepository, times(1)).findByExternalId(externalId);
    }

    @Test
    void removeByExternalId_shouldDeleteExpansion_whenExpansionExists() {
        // Given
        when(expansionRepository.findByExternalId(externalId)).thenReturn(Optional.of(expansion));
        doNothing().when(expansionRepository).delete(expansion);

        // When
        expansionService.removeByExternalId(externalId);

        // Then
        verify(expansionRepository, times(1)).findByExternalId(externalId);
        verify(expansionRepository, times(1)).delete(expansion);
    }

    @Test
    void removeByExternalId_shouldThrowResourceNotFoundException_whenExpansionDoesNotExist() {
        // Given
        when(expansionRepository.findByExternalId(externalId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                expansionService.removeByExternalId(externalId));
        assertEquals("Expansion with ID " + externalId + " not found", exception.getMessage());
        verify(expansionRepository, times(1)).findByExternalId(externalId);
        verifyNoMoreInteractions(expansionRepository);
    }

    @Test
    void updateByExternalId_shouldReturnUpdatedExpansionResponse_whenExpansionExists() {
        // Given
        ExpansionRequest updateRequest = new ExpansionRequest(externalId, expansion.getName(), expansion.getSerieName(), expansion.getReleaseDate().plusDays(1), expansion.getTotalCards(), expansion.getLogoUrl());
        
        when(expansionRepository.findByExternalId(externalId)).thenReturn(Optional.of(expansion));

        // When
        ExpansionResponse result = expansionService.updateByExternalId(externalId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(updateRequest.name(), result.name());
        assertEquals(updateRequest.serieName(), result.serieName());
        
        verify(expansionRepository, times(1)).findByExternalId(externalId);
    }

    @Test
    void updateByExternalId_shouldThrowResourceNotFoundException_whenExpansionDoesNotExist() {
        // Given
        when(expansionRepository.findByExternalId(externalId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                expansionService.updateByExternalId(externalId, expansionRequest));
        assertEquals("Expansion with ID " + externalId + " not found", exception.getMessage());
        verify(expansionRepository, times(1)).findByExternalId(externalId);
    }

    @Test
    void getAllExpansions_shouldReturnListOfExpansionResponses_whenExpansionsExist() {
        // Given
        when(expansionRepository.findAll()).thenReturn(Collections.singletonList(expansion));

        // When
        List<ExpansionResponse> result = expansionService.getAllExpansions();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(expansion.getExternalId(), result.get(0).externalId());
        
        verify(expansionRepository, times(1)).findAll();
    }

    @Test
    void getAllExpansions_shouldReturnEmptyList_whenNoExpansionsExist() {
        // Given
        when(expansionRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<ExpansionResponse> result = expansionService.getAllExpansions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(expansionRepository, times(1)).findAll();
    }
}
