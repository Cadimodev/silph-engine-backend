package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.InventoryCard;
import com.silphengine.domain.enums.CardCondition;
import com.silphengine.domain.enums.CardFinish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryCardRepository extends JpaRepository<InventoryCard, UUID> {

    List<InventoryCard> findByOwnerId(UUID ownerId);

    List<InventoryCard> findByOwnerIdAndCardId(UUID ownerId, UUID cardId);
    
    Optional<InventoryCard> findByIdAndOwnerId(UUID id, UUID ownerId);

    Optional<InventoryCard> findByOwnerIdAndCardIdAndCardConditionAndCardFinish(
            UUID ownerId, UUID cardId, CardCondition cardCondition, CardFinish cardFinish);
}
