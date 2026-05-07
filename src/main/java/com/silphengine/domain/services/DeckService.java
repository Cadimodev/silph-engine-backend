package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.DeckRequest;
import com.silphengine.domain.dto.responses.DeckResponse;

import java.util.List;
import java.util.UUID;

public interface DeckService {

    DeckResponse createDeck(DeckRequest deckRequest, UUID ownerId);

    DeckResponse getByIdAndOwnerID(UUID deckId, UUID ownerId);

    List<DeckResponse> getByOwnerId(UUID ownerId);

    List<DeckResponse> getByOwnerIdAndDeckName(UUID ownerId, String deckName);

    DeckResponse updateDeck(UUID deckId, DeckRequest deckRequest, UUID ownerId);

    void deleteDeck(UUID deckId, UUID ownerID);
}
