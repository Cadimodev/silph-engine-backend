package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.InventoryCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryCardRepository extends JpaRepository<InventoryCard, UUID> {

    public List<InventoryCard> findByOwnerId(UUID ownerId);

    public Optional<InventoryCard> findByOwnerIdAndCardId(UUID ownerId, UUID cardId);
}
