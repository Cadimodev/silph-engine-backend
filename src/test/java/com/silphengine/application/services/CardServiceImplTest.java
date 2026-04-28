package com.silphengine.application.services;

import com.silphengine.application.mappers.CardMapper;
import com.silphengine.application.mappers.CardMapperImpl;
import com.silphengine.domain.dto.requests.CardRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.BadRequestException;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.CardService;
import com.silphengine.infrastructure.repositories.CardRepository;
import com.silphengine.infrastructure.repositories.ExpansionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ExpansionRepository expansionRepository;

    private CardService cardService;

    private CardResponse cardResponse;

    private CardRequest cardRequest;

    private String externalCardId;

    private String externalExpansionId;

    private Card card;

    private Expansion expansion;

    @BeforeEach
    void setUp() {
        CardMapper cardMapper = new CardMapperImpl();
        cardService = new CardServiceImpl(cardMapper, cardRepository, expansionRepository);

        externalExpansionId = "sv02";
        String expansionName = "Paldea Evolved";
        String serieName = "Scarlet & Violet";
        LocalDate releaseDate = LocalDate.of(2023, 6, 9);
        int totalCards = 279;
        String expansionLogoUrl = "https://assets.tcgdex.net/en/sv/sv02/logo";

        expansion = Expansion.builder()
                .externalId(externalExpansionId)
                .name(expansionName)
                .serieName(serieName)
                .releaseDate(releaseDate)
                .totalCards(totalCards)
                .logoUrl(expansionLogoUrl)
                .build();

        externalCardId = "sv02-203";
        String name = "Magikarp";
        String rarity = "Illustration rare";
        String cardCategory = CardCategory.POKEMON.toString();
        List<String> types = List.of(CardType.WATER.toString());
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203";

        cardRequest = new CardRequest(externalCardId, name, externalExpansionId, rarity, cardCategory, types, imageUrl);
        card = cardMapper.toEntity(cardRequest, expansion);
    }

    @Test
    void createCard_shouldReturnCardResponse_whenCardIsCreatedSuccessfully() {

        // Given
        when(expansionRepository.findByExternalId(externalExpansionId)).thenReturn(Optional.of(expansion));
        when(cardRepository.findByExternalId(externalCardId)).thenReturn(Optional.empty());
        when(cardRepository.save((any(Card.class)))).thenReturn(card);

        // When
        CardResponse result = cardService.createCard(cardRequest);

        // Then
        assertNotNull(result);
        assertEquals(card.getExternalId(), result.externalId());
        assertEquals(card.getName(), result.name());
        assertEquals(card.getExpansion().getExternalId(), result.expansionExternalId());

        verify(expansionRepository, times(1)).findByExternalId(externalExpansionId);
        verify(cardRepository, times(1)).findByExternalId(externalCardId);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCard_shouldThrowDuplicateResourceException_whenCardAlreadyExists() {

        // Given
        when(expansionRepository.findByExternalId(externalExpansionId)).thenReturn(Optional.of(expansion));
        when(cardRepository.findByExternalId(externalCardId)).thenReturn(Optional.of(card));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> cardService.createCard(cardRequest));
        assertEquals("Card already exists with ID: " + externalCardId, exception.getMessage());

        verify(expansionRepository, times(1)).findByExternalId(externalExpansionId);
        verify(cardRepository, times(1)).findByExternalId(externalCardId);
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    void createCard_shouldResourceNotFoundException_whenExpansionDoesNotExists() {

        // Given
        when(expansionRepository.findByExternalId(externalExpansionId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.createCard(cardRequest));
        assertEquals("Expansion with ID " + externalExpansionId + " not found", exception.getMessage());

        verify(expansionRepository, times(1)).findByExternalId(externalExpansionId);
        verifyNoMoreInteractions(expansionRepository);
    }

    @Test
    void getByExternalId_shouldReturnCardResponse_whenCardExists() {

        // Given
        when(cardRepository.findByExternalId(externalCardId)).thenReturn(Optional.of(card));

        // When
        CardResponse result = cardService.getByExternalId(externalCardId);

        // Then
        assertNotNull(result);
        assertEquals(card.getExternalId(), result.externalId());
        assertEquals(card.getExpansion().getExternalId(), result.expansionExternalId());

        verify(cardRepository, times(1)).findByExternalId(externalCardId);
    }

    @Test
    void getByExternalId_shouldThrowResourceNotFoundException_whenCardDoesNotExists() {

        // Given
        when(cardRepository.findByExternalId(externalCardId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.getByExternalId(externalCardId));

        assertEquals("Card with ID: " + externalCardId + " not found", exception.getMessage());

        verify(cardRepository, times(1)).findByExternalId(externalCardId);
    }

    @Test
    void getAllCards_shouldReturnListOfCardResponse() {

        // Given
        when(cardRepository.findAll()).thenReturn(List.of(card));

        // When
        List<CardResponse> result = cardService.getAllCards();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(cardRepository, times(1)).findAll();
    }

    @Test
    void getByExternalExpansionId_shouldReturnListOfCardResponse() {

        // Given
        when(cardRepository.findByExpansion_ExternalId(externalExpansionId)).thenReturn(List.of(card));

        // When
        List<CardResponse> result = cardService.getByExternalExpansionId(externalExpansionId);

        // Then
        assertNotNull(result);
        verify(cardRepository, times(1)).findByExpansion_ExternalId(externalExpansionId);
    }

    @Test
    void updateByExternalId_shouldReturnCardResponse_whenCardExists() {

        // Given
        CardRequest updateRequest = new CardRequest(externalCardId, card.getName(), card.getExpansion().getExternalId(), card.getRarity(), "TRAINER", List.of("Water"), card.getImageUrl());
        when(cardRepository.findByExternalId(externalCardId)).thenReturn(Optional.of(card));

        // When
        CardResponse result = cardService.updateByExternalId(externalCardId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(updateRequest.externalId(), result.externalId());
        assertEquals(updateRequest.name(), result.name());

        verify(cardRepository, times(1)).findByExternalId(externalCardId);
    }

    @Test
    void updateByExternalId_shouldThrowResourceNotFoundException_whenCardDoesNotExists() {

        // Given
        CardRequest updateRequest = new CardRequest(externalCardId, card.getName(), card.getExpansion().getExternalId(), card.getRarity(), "TRAINER", List.of("Water"), card.getImageUrl());
        when(cardRepository.findByExternalId(externalCardId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.updateByExternalId(externalCardId, updateRequest));
        assertEquals("Card with ID: " + externalCardId + " not found", exception.getMessage());

        verify(cardRepository, times(1)).findByExternalId(externalCardId);
    }

    @Test
    void updateByExternalId_shouldThrowBadRequestException_whenExpansionIDIsDifferent() {

        // Given
        CardRequest updateRequest = new CardRequest(externalCardId, card.getName(), "diff-Ex-ID", card.getRarity(), "TRAINER", List.of("Water"), card.getImageUrl());
        when(cardRepository.findByExternalId(externalCardId)).thenReturn(Optional.of(card));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> cardService.updateByExternalId(externalCardId, updateRequest));
        assertEquals("A card's expansion cannot be changed. The request's expansion ID ("
                + updateRequest.expansionExternalId() + ") does not match the existing card's expansion ID ("
                + card.getExpansion().getExternalId() + ").", exception.getMessage());

        verify(cardRepository, times(1)).findByExternalId(externalCardId);
    }

    @Test
    void deleteByExternalId_shouldDeleteCard_whenCardExists() {

        // Given
        when(cardRepository.findByExternalId(externalCardId)).thenReturn(Optional.of(card));
        doNothing().when(cardRepository).delete(card);

        // When
        cardService.deleteByExternalId(externalCardId);

        // Then
        verify(cardRepository, times(1)).findByExternalId(externalCardId);
        verify(cardRepository, times(1)).delete(card);
    }

    @Test
    void deleteByExternalId_shouldThrowResourceNotFoundException_whenCardDoesNotExists() {

        // Given
        when(cardRepository.findByExternalId(externalCardId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.deleteByExternalId(externalCardId));
        assertEquals("Card with ID: " + externalCardId + " not found", exception.getMessage());

        verify(cardRepository, times(1)).findByExternalId(externalCardId);
        verifyNoMoreInteractions(cardRepository);
    }
}
