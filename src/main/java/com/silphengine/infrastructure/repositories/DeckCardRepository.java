package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.DeckCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeckCardRepository extends JpaRepository<DeckCard, UUID> {

    public List<DeckCard> findByDeckId(UUID deckId);
}
