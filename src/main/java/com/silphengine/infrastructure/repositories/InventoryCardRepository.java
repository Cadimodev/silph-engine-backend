package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.InventoryCard;
import com.silphengine.domain.enums.CardCondition;
import com.silphengine.domain.enums.CardFinish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryCardRepository extends JpaRepository<InventoryCard, UUID> {

    Page<InventoryCard> findByOwnerId(UUID ownerId, Pageable pageable);

    List<InventoryCard> findByOwnerIdAndCardId(UUID ownerId, UUID cardId);

    @Query("SELECT ic FROM InventoryCard ic WHERE ic.id = (" +
            "  SELECT MIN(sub.id) FROM InventoryCard sub WHERE sub.card.id = ic.card.id AND sub.owner.id = :ownerId" +
            ")")
    Page<InventoryCard> findUniqueCardsByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);
    
    Optional<InventoryCard> findByIdAndOwnerId(UUID id, UUID ownerId);

    Optional<InventoryCard> findByOwnerIdAndCardIdAndCardConditionAndCardFinish(
            UUID ownerId, UUID cardId, CardCondition cardCondition, CardFinish cardFinish);
}
