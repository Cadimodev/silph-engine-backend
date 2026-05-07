package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeckRepository extends JpaRepository<Deck, UUID> {

    public Optional<Deck> findByIdAndOwnerId(UUID deckId, UUID ownerId);

    public List<Deck> findByOwnerId(UUID ownerId);

    public Optional<Deck> findByOwnerIdAndName(UUID ownerId, String name);
}
