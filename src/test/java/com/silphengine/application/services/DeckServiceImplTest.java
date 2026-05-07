package com.silphengine.application.services;

import com.silphengine.application.config.FormatProperties;
import com.silphengine.application.mappers.DeckMapper;
import com.silphengine.domain.dto.requests.DeckCardRequest;
import com.silphengine.domain.dto.requests.DeckRequest;
import com.silphengine.domain.dto.responses.DeckResponse;
import com.silphengine.domain.entities.*;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.DeckService;
import com.silphengine.infrastructure.repositories.CardRepository;
import com.silphengine.infrastructure.repositories.DeckRepository;
import com.silphengine.infrastructure.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeckServiceImplTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckMapper deckMapper;

    @Mock
    private FormatProperties formatProperties;

    private DeckService deckService;


    private Deck deck;
    private DeckResponse deckResponse;
    private User owner;
    private Card card;
    private DeckRequest deckRequest;
    private DeckCard deckCard;


    @BeforeEach
    void setUp() {

        deckService = new DeckServiceImpl(deckMapper, deckRepository, userRepository, cardRepository, formatProperties);

        owner = User.builder()
                .id(UUID.randomUUID())
                .nickname("Ash")
                .build();

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

        deckRequest = new DeckRequest("Test Deck", List.of(new DeckCardRequest(card.getId(), 4)));

        deck = Deck.builder()
                .id(UUID.randomUUID())
                .name(deckRequest.name())
                .owner(owner)
                .isLegal(false)
                .cards(List.of(deckCard))
                .build();

        deckResponse = new DeckResponse(
                deck.getId(),
                owner.getId(),
                deck.getName(),
                deck.getIsLegal(),
                List.of()
        );
    }

    @Test
    void createDeck_shouldReturnDeckResponse_whenDeckIsCreatedSuccessfully() {

        // Given
        when(deckRepository.findByOwnerIdAndName(any(UUID.class), eq(deckRequest.name()))).thenReturn(Optional.empty());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(owner));
        when(cardRepository.findById(any(UUID.class))).thenReturn(Optional.of(card));

        when(deckMapper.toEntity(eq(deckRequest), eq(owner), anyList())).thenReturn(deck);
        when(deckMapper.toResponse(eq(deck))).thenReturn(deckResponse);

        when(formatProperties.getStandardValidMarks()).thenReturn(List.of("G", "H", "I"));

        when(deckRepository.save(any(Deck.class))).thenReturn(deck);

        // When
        DeckResponse result = deckService.createDeck(deckRequest, owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(deckResponse.name(), result.name());
        assertEquals(deckResponse.id(), result.id());

        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), deckRequest.name());
        verify(userRepository, times(1)).findById(owner.getId());
        verify(cardRepository, times(1)).findById(card.getId());
        verify(deckMapper, times(1)).toEntity(eq(deckRequest), eq(owner), anyList());
        verify(deckRepository, times(1)).save(deck);
        verify(deckMapper, times(1)).toResponse(deck);
        verify(formatProperties, times(1)).getStandardValidMarks();
    }

    @Test
    void createDeck_shouldThrowDuplicateResource_whenDeckAlreadyExists() {

        // Given
        when(deckRepository.findByOwnerIdAndName(any(UUID.class), eq(deckRequest.name()))).thenReturn(Optional.of(deck));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> deckService.createDeck(deckRequest, owner.getId()));
        assertEquals("Deck already exists with that name for this user", exception.getMessage());

        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), deckRequest.name());
    }

    @Test
    void createDeck_shouldThrowResourceNotFound_whenUserDoesNotExists() {

        // Given
        when(deckRepository.findByOwnerIdAndName(any(UUID.class), eq(deckRequest.name()))).thenReturn(Optional.empty());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.createDeck(deckRequest, owner.getId()));
        assertEquals("User with ID: " + owner.getId() + " not found", exception.getMessage());

        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), deckRequest.name());
        verify(userRepository, times(1)).findById(owner.getId());
    }

    @Test
    void createDeck_shouldThrowResourceNotFound_whenCardDoesNotExists() {

        // Given
        when(deckRepository.findByOwnerIdAndName(any(UUID.class), eq(deckRequest.name()))).thenReturn(Optional.empty());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(owner));
        when(cardRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.createDeck(deckRequest, owner.getId()));
        assertEquals("Card with ID: " + card.getId() + " not found", exception.getMessage());

        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), deckRequest.name());
        verify(userRepository, times(1)).findById(owner.getId());
        verify(cardRepository, times(1)).findById(card.getId());
    }

    @Test
    void getByIdAndOwnerID_shouldReturnDeckResponse_whenDeckExists() {

        // Given
        when(deckRepository.findByIdAndOwnerId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(deck));
        when(deckMapper.toResponse(eq(deck))).thenReturn(deckResponse);

        // When
        DeckResponse result = deckService.getByIdAndOwnerID(deck.getId(), owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(deckResponse.name(), result.name());
        assertEquals(deckResponse.id(), result.id());
        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
    }

    @Test
    void getByIdAndOwnerID_shouldThrowResourceNotFound_whenDeckDoesNotExists() {

        // Given
        when(deckRepository.findByIdAndOwnerId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.getByIdAndOwnerID(deck.getId(), owner.getId()));
        assertEquals("Deck with ID: " + deck.getId() + " not found" , exception.getMessage());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
    }

    @Test
    void getByOwnerId_shouldReturnListOfDeckResponse() {

        // Given
        when(deckRepository.findByOwnerId(any(UUID.class))).thenReturn(List.of(deck));
        when(deckMapper.toResponse(eq(deck))).thenReturn(deckResponse);


        // When
        List<DeckResponse> result = deckService.getByOwnerId(owner.getId());

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(deckRepository, times(1)).findByOwnerId(owner.getId());
    }

    @Test
    void getByOwnerIdAndDeckName_shouldReturnDeckResponse_whenDeckExists() {

        // Given
        when(deckRepository.findByOwnerIdAndName(any(UUID.class), eq(deckRequest.name()))).thenReturn(Optional.of(deck));
        when(deckMapper.toResponse(eq(deck))).thenReturn(deckResponse);


        // When
        List<DeckResponse> result = deckService.getByOwnerIdAndDeckName(owner.getId(), deckRequest.name());

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(deckResponse.name(), result.getFirst().name());
        assertEquals(deckResponse.id(), result.getFirst().id());
        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), deckRequest.name());
    }

    @Test
    void updateDeck_shouldReturnDeckResponse_whenDeckExists() {

        // Given
        DeckRequest updateRequest = new DeckRequest("Update Test Deck", List.of(new DeckCardRequest(card.getId(), 4)));

        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.of(deck));
        when(deckRepository.findByOwnerIdAndName(owner.getId(), updateRequest.name())).thenReturn(Optional.empty());
        when(cardRepository.findById(any(UUID.class))).thenReturn(Optional.of(card));
        when(formatProperties.getStandardValidMarks()).thenReturn(List.of("G", "H", "I"));

        doNothing().when(deckMapper).updateEntityFromRequest(any(Deck.class), any(DeckRequest.class), anyList());

        // Must return the same object it receives
        when(deckRepository.save(any(Deck.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(deckMapper.toResponse(any(Deck.class))).thenReturn(
                new DeckResponse(deck.getId(), owner.getId(), updateRequest.name(), false, List.of())
        );

        // When
        DeckResponse result = deckService.updateDeck(deck.getId(), updateRequest, owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(updateRequest.name(), result.name());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), updateRequest.name());
        verify(deckRepository, times(1)).save(deck);
    }

    @Test
    void updateDeck_shouldThrowResourceNotFound_whenDeckDoesNotExists() {

        // Given
        DeckRequest updateRequest = new DeckRequest("Update Test Deck", List.of(new DeckCardRequest(card.getId(), 4)));

        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.updateDeck(deck.getId(), updateRequest, owner.getId()));

        assertEquals("Deck with ID: " + deck.getId() + " not found", exception.getMessage());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
    }

    @Test
    void updateDeck_shouldThrowDuplicateResource_whenDeckAlreadyExistsWithNewName() {

        // Given
        DeckRequest updateRequest = new DeckRequest("Existing Deck Name", List.of(new DeckCardRequest(card.getId(), 4)));

        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.of(deck));
        when(deckRepository.findByOwnerIdAndName(owner.getId(), updateRequest.name())).thenReturn(Optional.of(deck));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> deckService.updateDeck(deck.getId(), updateRequest, owner.getId()));

        assertEquals("Deck already exists with the name: " + updateRequest.name(), exception.getMessage());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), updateRequest.name());
    }


    @Test
    void updateDeck_shouldThrowResourceNotFound_whenCardDoesNotExists() {

        // Given
        DeckRequest updateRequest = new DeckRequest("Existing Deck Name", List.of(new DeckCardRequest(card.getId(), 4)));

        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.of(deck));
        when(deckRepository.findByOwnerIdAndName(owner.getId(), updateRequest.name())).thenReturn(Optional.empty());
        when(cardRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.updateDeck(deck.getId(), updateRequest, owner.getId()));

        assertEquals("Card with ID: " + card.getId() + " not found", exception.getMessage());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), updateRequest.name());
        verify(cardRepository, times(1)).findById(card.getId());
    }

    @Test
    void deleteDeck_shouldDeleteDeck_whenDeckExists() {

        // Given
        when(deckRepository.findByIdAndOwnerId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(deck));
        doNothing().when(deckRepository).delete(deck);

        // When
        deckService.deleteDeck(deck.getId(), owner.getId());

        // Then
        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
        verify(deckRepository, times(1)).delete(deck);
    }

    @Test
    void deleteDeck_shouldThrowResourceNotFoundException_whenDeckDoesNotExists() {

        // Given
        when(deckRepository.findByIdAndOwnerId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.deleteDeck(deck.getId(), owner.getId()));
        assertEquals("Deck with ID: " + deck.getId() + " not found", exception.getMessage());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
        verifyNoMoreInteractions(deckRepository);
    }

}
