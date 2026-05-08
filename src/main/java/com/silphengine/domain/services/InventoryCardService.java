package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.InventoryCardRequest;
import com.silphengine.domain.dto.requests.UpdateInventoryCardRequest;
import com.silphengine.domain.dto.responses.InventoryCardResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryCardService {

    InventoryCardResponse createInventoryCard(InventoryCardRequest request, UUID ownerId);

    List<InventoryCardResponse> getCollection(UUID ownerID);

    List<InventoryCardResponse> getInventoryCardsByCardId(UUID cardId, UUID ownerID);

    InventoryCardResponse getInventoryCard(UUID inventoryCardId, UUID ownerID);

    InventoryCardResponse updateInventoryCard(UUID inventoryCardId, UpdateInventoryCardRequest request, UUID ownerId);

    void deleteInventoryCard(UUID inventoryCardId, UUID ownerID);

}
