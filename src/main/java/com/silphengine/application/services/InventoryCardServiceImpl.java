package com.silphengine.application.services;

import com.silphengine.application.mappers.InventoryCardMapper;
import com.silphengine.domain.dto.requests.InventoryCardRequest;
import com.silphengine.domain.dto.requests.UpdateInventoryCardRequest;
import com.silphengine.domain.dto.responses.InventoryCardResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.InventoryCard;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.InventoryCardService;
import com.silphengine.infrastructure.repositories.CardRepository;
import com.silphengine.infrastructure.repositories.InventoryCardRepository;
import com.silphengine.infrastructure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryCardServiceImpl implements InventoryCardService {

    private final InventoryCardMapper inventoryCardMapper;

    private final InventoryCardRepository inventoryCardRepository;

    private final UserRepository userRepository;

    private final CardRepository cardRepository;

    @Override
    public InventoryCardResponse createInventoryCard(InventoryCardRequest request, UUID ownerId) {

        User user = userRepository.findById(ownerId).orElseThrow(
                () -> new ResourceNotFoundException("User with ID: " + ownerId + " not found"));

        Card card = cardRepository.findById(request.cardId()).orElseThrow(
                () -> new ResourceNotFoundException("Card with ID: " + request.cardId() + " not found"));

        InventoryCard inventoryCardToCreate = inventoryCardMapper.toEntity(request, user, card);

        // Check if an exact match already exists to avoid unique constraint violations
        Optional<InventoryCard> existingCardOpt = inventoryCardRepository
                .findByOwnerIdAndCardIdAndCardConditionAndCardFinish(
                        ownerId,
                        card.getId(),
                        inventoryCardToCreate.getCardCondition(),
                        inventoryCardToCreate.getCardFinish()
                );

        if (existingCardOpt.isPresent()) {
            InventoryCard existingCard = existingCardOpt.get();
            existingCard.changeQuantity(existingCard.getQuantity() + inventoryCardToCreate.getQuantity());
            return inventoryCardMapper.toResponse(inventoryCardRepository.save(existingCard));
        }

        return inventoryCardMapper.toResponse(inventoryCardRepository.save(inventoryCardToCreate));
    }

    @Override
    public List<InventoryCardResponse> getCollection(UUID ownerID) {
        
        // TODO: Add pagination to this
        return inventoryCardRepository.findByOwnerId(ownerID)
                .stream()
                .map(inventoryCardMapper::toResponse)
                .toList();
    }

    @Override
    public List<InventoryCardResponse> getInventoryCardsByCardId(UUID cardId, UUID ownerID) {
        return inventoryCardRepository.findByOwnerIdAndCardId(ownerID, cardId)
                .stream()
                .map(inventoryCardMapper::toResponse)
                .toList();
    }

    @Override
    public InventoryCardResponse getInventoryCard(UUID inventoryCardId, UUID ownerID) {

        InventoryCard inventoryCard = inventoryCardRepository.findByIdAndOwnerId(inventoryCardId, ownerID).orElseThrow(
                () -> new ResourceNotFoundException("Inventory Card with ID: " + inventoryCardId + " not found"));

        return inventoryCardMapper.toResponse(inventoryCard);

    }

    @Override
    public InventoryCardResponse updateInventoryCard(UUID inventoryCardId, UpdateInventoryCardRequest request, UUID ownerID) {

        InventoryCard inventoryCard  = inventoryCardRepository.findById(inventoryCardId).orElseThrow(
                () -> new ResourceNotFoundException("Inventory Card with ID: " + inventoryCardId + " not found"));

        if (!inventoryCard.getOwner().getId().equals(ownerID)) {
            throw new AuthorizationDeniedException("You do not have permission to access this resource.");
        }

        inventoryCardMapper.updateEntityFromRequest(inventoryCard, request);

        return inventoryCardMapper.toResponse(inventoryCardRepository.save(inventoryCard));

    }

    @Override
    public void deleteInventoryCard(UUID inventoryCardId, UUID ownerID) {

        InventoryCard inventoryCard  = inventoryCardRepository.findById(inventoryCardId).orElseThrow(
                () -> new ResourceNotFoundException("Inventory Card with ID: " + inventoryCardId + " not found"));

        if (!inventoryCard.getOwner().getId().equals(ownerID)) {
            throw new AuthorizationDeniedException("You do not have permission to access this resource.");
        }

        inventoryCardRepository.delete(inventoryCard);

    }
}
