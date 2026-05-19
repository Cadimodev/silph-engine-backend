package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByExternalId(String externalId);

    Page<Card> findByExpansion_ExternalId(String expansionExternalId, Pageable pageable);

    List<Card> findByExpansion(Expansion expansion);
}
