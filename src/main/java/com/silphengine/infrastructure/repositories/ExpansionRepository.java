package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Expansion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExpansionRepository extends JpaRepository<Expansion, UUID> {

    public Optional<Expansion> findByExternalId(String externalId);
}
