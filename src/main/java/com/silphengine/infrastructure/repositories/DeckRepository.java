package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Deck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeckRepository extends JpaRepository<Deck, UUID> {

    public Optional<Deck> findByIdAndOwnerId(UUID deckId, UUID ownerId);

    public Page<Deck> findByOwnerId(UUID ownerId, Pageable pageable);

    public Optional<Deck> findByOwnerIdAndName(UUID ownerId, String name);
}
