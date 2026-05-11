package com.silphengine.application.services;

import com.silphengine.application.mappers.InventoryCardMapper;
import com.silphengine.domain.dto.requests.InventoryCardRequest;
import com.silphengine.domain.dto.requests.UpdateInventoryCardRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.dto.responses.InventoryCardResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.InventoryCard;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardCondition;
import com.silphengine.domain.enums.CardFinish;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.InventoryCardService;
import com.silphengine.infrastructure.repositories.CardRepository;
import com.silphengine.infrastructure.repositories.InventoryCardRepository;
import com.silphengine.infrastructure.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
public class InventoryCardServiceImplTest {

    @Mock
    private InventoryCardRepository inventoryCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private InventoryCardMapper inventoryCardMapper;

    private InventoryCardService inventoryCardService;

    private InventoryCard inventoryCard;
    private User owner;
    private Card card;
    private CardResponse cardResponse;
    private InventoryCardRequest inventoryCardRequest;
    private InventoryCardResponse inventoryCardResponse;

    @BeforeEach
    void setUp() {

        inventoryCardService = new InventoryCardServiceImpl(inventoryCardMapper, inventoryCardRepository, userRepository, cardRepository);

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

        cardResponse = new CardResponse(
                card.getExternalId(), card.getName(), card.getRarity(),
                card.getCardCategory(), card.getTypes(), card.getImageUrl(), "expansionExternalID", card.getRegulationMark());

        inventoryCard = InventoryCard.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .card(card)
                .quantity(1)
                .cardCondition(CardCondition.NEAR_MINT)
                .cardFinish(CardFinish.NORMAL)
                .build();

        inventoryCardRequest = new InventoryCardRequest(card.getId(), 1, "near mint", "normal");
        inventoryCardResponse = new InventoryCardResponse(inventoryCard.getId(), cardResponse, inventoryCard.getQuantity(), inventoryCard.getCardCondition(), inventoryCard.getCardFinish());
    }

    @Test
    void createInventoryCard_shouldReturnInventoryCardResponse_whenInventoryCardIsCreatedSuccessfully() {

        // Given
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(owner));
        when(cardRepository.findById(any(UUID.class))).thenReturn(Optional.of(card));
        when(inventoryCardRepository.
                findByOwnerIdAndCardIdAndCardConditionAndCardFinish(any(UUID.class), any(UUID.class), any(CardCondition.class), any(CardFinish.class)))
                .thenReturn(Optional.empty());
        when(inventoryCardMapper.toEntity(any(InventoryCardRequest.class), any(User.class), any(Card.class))).thenReturn(inventoryCard);
        when(inventoryCardRepository.save(any(InventoryCard.class))).thenReturn(inventoryCard);
        when(inventoryCardMapper.toResponse(any(InventoryCard.class))).thenReturn(inventoryCardResponse);

        // When
        InventoryCardResponse result = inventoryCardService.createInventoryCard(inventoryCardRequest, owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(inventoryCardResponse.quantity(), result.quantity());
        assertEquals(inventoryCardResponse.cardCondition(), result.cardCondition());
        assertEquals(inventoryCardResponse.cardFinish(), result.cardFinish());

        verify(userRepository, times(1)).findById(owner.getId());
        verify(cardRepository, times(1)).findById(card.getId());
        verify(inventoryCardRepository, times(1))
                .findByOwnerIdAndCardIdAndCardConditionAndCardFinish(owner.getId(), card.getId(), inventoryCard.getCardCondition(), inventoryCard.getCardFinish());
        verify(inventoryCardMapper, times(1)).toEntity(inventoryCardRequest, owner, card);
        verify(inventoryCardRepository, times(1)).save(inventoryCard);
        verify(inventoryCardMapper, times(1)).toResponse(inventoryCard);

    }

    @Test
    void createInventoryCard_shouldReturnResourceNotFound_whenOwnerDoesNotExists() {

        // Given
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> inventoryCardService.createInventoryCard(inventoryCardRequest, owner.getId()));

        // Then
        assertEquals("User with ID: " + owner.getId() + " not found", exception.getMessage());

        verify(userRepository, times(1)).findById(owner.getId());
    }

    @Test
    void createInventoryCard_shouldReturnResourceNotFound_whenCardDoesNotExists() {

        // Given
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(owner));
        when(cardRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> inventoryCardService.createInventoryCard(inventoryCardRequest, owner.getId()));

        // Then
        assertEquals("Card with ID: " + card.getId() + " not found", exception.getMessage());

        verify(userRepository, times(1)).findById(owner.getId());
        verify(cardRepository, times(1)).findById(card.getId());
    }

    @Test
    void createInventoryCard_shouldUpdateQuantity_whenInventoryCardAlreadyExists() {

        // Given

        int initialQuantity = inventoryCard.getQuantity();
        int expectedQuantity = initialQuantity + 1;
        InventoryCardResponse expectedResponse = new InventoryCardResponse(
                inventoryCard.getId(),
                cardResponse,
                expectedQuantity,
                inventoryCard.getCardCondition(),
                inventoryCard.getCardFinish()
        );

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(owner));
        when(cardRepository.findById(any(UUID.class))).thenReturn(Optional.of(card));

        when(inventoryCardMapper.toEntity(any(InventoryCardRequest.class), any(User.class), any(Card.class)))
                .thenReturn(inventoryCard);

        when(inventoryCardRepository.findByOwnerIdAndCardIdAndCardConditionAndCardFinish(
                any(UUID.class), any(UUID.class), any(CardCondition.class), any(CardFinish.class)))
                .thenReturn(Optional.of(inventoryCard));

        when(inventoryCardRepository.save(any(InventoryCard.class))).thenReturn(inventoryCard);
        when(inventoryCardMapper.toResponse(any(InventoryCard.class))).thenReturn(expectedResponse);

        // When
        InventoryCardResponse result = inventoryCardService.createInventoryCard(inventoryCardRequest, owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(expectedQuantity, result.quantity());

        verify(inventoryCardRepository, times(1)).save(inventoryCard);
        verify(inventoryCardMapper, times(1)).toResponse(inventoryCard);
    }

    @Test
    void getCollection_shouldReturnListOfInventoryCardResponse() {

        // Given
        when(inventoryCardRepository.findByOwnerId(eq(owner.getId()))).thenReturn(List.of(inventoryCard));

        // When
        List<InventoryCardResponse> result = inventoryCardService.getCollection(owner.getId());

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());

        verify(inventoryCardRepository, times(1)).findByOwnerId(owner.getId());
    }

    @Test
    void getInventoryCardsByCardId_shouldReturnListOfInventoryCardResponse() {

        // Given
        when(inventoryCardRepository.findByOwnerIdAndCardId(eq(owner.getId()), eq(card.getId()))).thenReturn(List.of(inventoryCard));

        // When
        List<InventoryCardResponse> result = inventoryCardService.getInventoryCardsByCardId(card.getId(), owner.getId());

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());

        verify(inventoryCardRepository, times(1)).findByOwnerIdAndCardId(owner.getId(), card.getId());
    }

    @Test
    void getInventoryCard_shouldReturnInventoryCardResponse_whenInventoryCardExists() {

        // Given
        when(inventoryCardRepository.findByIdAndOwnerId(eq(inventoryCard.getId()), eq(owner.getId()))).thenReturn(Optional.of(inventoryCard));
        when(inventoryCardMapper.toResponse(any(InventoryCard.class))).thenReturn(inventoryCardResponse);

        // When
        InventoryCardResponse result = inventoryCardService.getInventoryCard(inventoryCard.getId(), owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(inventoryCard.getId(), result.id());

        verify(inventoryCardRepository, times(1)).findByIdAndOwnerId(inventoryCard.getId(), owner.getId());
    }

    @Test
    void getInventoryCard_shouldReturnResourceNotFound_whenInventoryCardDoesNotExists() {

        // Given
        when(inventoryCardRepository.findByIdAndOwnerId(eq(inventoryCard.getId()), eq(owner.getId()))).thenReturn(Optional.empty());

        // When
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> inventoryCardService.getInventoryCard(inventoryCard.getId(), owner.getId()));

        // Then
        assertEquals("Inventory Card with ID: " + inventoryCard.getId() + " not found", exception.getMessage());

        verify(inventoryCardRepository, times(1)).findByIdAndOwnerId(inventoryCard.getId(), owner.getId());
    }

    @Test
    void updateInventoryCard_shouldReturnInventoryCardResponse_whenUpdatedSuccessfully() {

        // Given
        int newQuantity = 5;
        UpdateInventoryCardRequest updateRequest = new UpdateInventoryCardRequest(newQuantity);
        
        InventoryCardResponse expectedResponse = new InventoryCardResponse(
                inventoryCard.getId(), 
                cardResponse, 
                newQuantity, 
                inventoryCard.getCardCondition(), 
                inventoryCard.getCardFinish()
        );

        when(inventoryCardRepository.findById(eq(inventoryCard.getId()))).thenReturn(Optional.of(inventoryCard));
        when(inventoryCardRepository.save(any(InventoryCard.class))).thenReturn(inventoryCard);
        when(inventoryCardMapper.toResponse(any(InventoryCard.class))).thenReturn(expectedResponse);

        // When
        InventoryCardResponse result = inventoryCardService.updateInventoryCard(inventoryCard.getId(), updateRequest, owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(newQuantity, result.quantity());
        assertEquals(inventoryCard.getId(), result.id());

        verify(inventoryCardRepository, times(1)).findById(inventoryCard.getId());
        verify(inventoryCardMapper, times(1)).updateEntityFromRequest(inventoryCard, updateRequest);
        verify(inventoryCardRepository, times(1)).save(inventoryCard);
        verify(inventoryCardMapper, times(1)).toResponse(inventoryCard);
    }

    @Test
    void updateInventoryCard_shouldThrowResourceNotFound_whenInventoryCardDoesNotExists() {

        // Given
        int newQuantity = 5;
        UpdateInventoryCardRequest updateRequest = new UpdateInventoryCardRequest(newQuantity);

        when(inventoryCardRepository.findById(eq(inventoryCard.getId()))).thenReturn(Optional.empty());

        // When
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> inventoryCardService.updateInventoryCard(inventoryCard.getId(), updateRequest, owner.getId()));

        // Then
        assertEquals("Inventory Card with ID: " + inventoryCard.getId() + " not found", exception.getMessage());

        verify(inventoryCardRepository, times(1)).findById(inventoryCard.getId());
    }

    @Test
    void updateInventoryCard_shouldThrowAuthorizationDenied_whenNotAuthorizedUser() {

        // Given
        int newQuantity = 5;
        UpdateInventoryCardRequest updateRequest = new UpdateInventoryCardRequest(newQuantity);

        when(inventoryCardRepository.findById(eq(inventoryCard.getId()))).thenReturn(Optional.of(inventoryCard));

        // When
        AuthorizationDeniedException exception = assertThrows(AuthorizationDeniedException.class,
                () -> inventoryCardService.updateInventoryCard(inventoryCard.getId(), updateRequest, UUID.randomUUID()));

        // Then
        assertEquals("You do not have permission to access this resource.", exception.getMessage());

        verify(inventoryCardRepository, times(1)).findById(inventoryCard.getId());
    }

    @Test
    void deleteInventoryCard_shouldDeleteInventoryCard_whenInventoryCardExists() {

        // Given
        when(inventoryCardRepository.findById(eq(inventoryCard.getId()))).thenReturn(Optional.of(inventoryCard));
        doNothing().when(inventoryCardRepository).delete(inventoryCard);

        // When
        inventoryCardService.deleteInventoryCard(inventoryCard.getId(), owner.getId());

        // Then
        verify(inventoryCardRepository, Mockito.times(1)).findById(inventoryCard.getId());
        verify(inventoryCardRepository, Mockito.times(1)).delete(inventoryCard);
    }

    @Test
    void deleteInventoryCard_shouldThrowResourceNotFound_whenInventoryCardDoesNotExists() {

        // Given
        when(inventoryCardRepository.findById(eq(inventoryCard.getId()))).thenReturn(Optional.empty());

        // When
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> inventoryCardService.deleteInventoryCard(inventoryCard.getId(), owner.getId()));

        // Then
        assertEquals("Inventory Card with ID: " + inventoryCard.getId() + " not found", exception.getMessage());

        verify(inventoryCardRepository, Mockito.times(1)).findById(inventoryCard.getId());
        verifyNoMoreInteractions(inventoryCardRepository);
    }

    @Test
    void deleteInventoryCard_shouldThrowAuthorizationDenied_whenNotAuthorizedUser() {

        // Given
        when(inventoryCardRepository.findById(eq(inventoryCard.getId()))).thenReturn(Optional.of(inventoryCard));

        // When
        AuthorizationDeniedException exception = assertThrows(AuthorizationDeniedException.class,
                () -> inventoryCardService.deleteInventoryCard(inventoryCard.getId(), UUID.randomUUID()));

        // Then
        assertEquals("You do not have permission to access this resource.", exception.getMessage());

        verify(inventoryCardRepository, Mockito.times(1)).findById(inventoryCard.getId());
        verifyNoMoreInteractions(inventoryCardRepository);
    }
}
