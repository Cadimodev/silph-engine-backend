package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByExternalId(String externalId);

    List<Card> findByExpansion_ExternalId(String expansionExternalId);

    List<Card> findByExpansionIn(Collection<Expansion> expansions);
}
