package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.DeckRequest;
import com.silphengine.domain.dto.responses.DeckResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DeckService {

    DeckResponse createDeck(DeckRequest deckRequest, UUID ownerId);

    DeckResponse getByIdAndOwnerID(UUID deckId, UUID ownerId);

    Page<DeckResponse> getByOwnerId(UUID ownerId, Pageable pageable);

    Page<DeckResponse> getByOwnerIdAndDeckName(UUID ownerId, String deckName, Pageable pageable);

    DeckResponse updateDeck(UUID deckId, DeckRequest deckRequest, UUID ownerId);

    void deleteDeck(UUID deckId, UUID ownerID);
}
