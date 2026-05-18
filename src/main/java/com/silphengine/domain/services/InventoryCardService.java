package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.InventoryCardRequest;
import com.silphengine.domain.dto.requests.UpdateInventoryCardRequest;
import com.silphengine.domain.dto.responses.InventoryCardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InventoryCardService {

    InventoryCardResponse createInventoryCard(InventoryCardRequest request, UUID ownerId);

    Page<InventoryCardResponse> getCollection(UUID ownerID, Pageable pageable);

    List<InventoryCardResponse> getInventoryCardsByCardId(UUID cardId, UUID ownerID);

    InventoryCardResponse getInventoryCard(UUID inventoryCardId, UUID ownerID);

    InventoryCardResponse updateInventoryCard(UUID inventoryCardId, UpdateInventoryCardRequest request, UUID ownerId);

    void deleteInventoryCard(UUID inventoryCardId, UUID ownerID);

}
