package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.DeckRequest;
import com.silphengine.domain.dto.responses.DeckResponse;

import java.util.List;
import java.util.UUID;

public interface DeckService {

    DeckResponse createDeck(DeckRequest deckRequest);

    List<DeckResponse> getByOwnerId(UUID ownerId);

    DeckResponse getByOwnerIdAndDeckName(UUID ownerId, String deckName);

    DeckResponse updateDeck(UUID deckId, DeckRequest deckRequest);

    void deleteDeck(UUID deckId);
}
