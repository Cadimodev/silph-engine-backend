package com.silphengine.application.services;

import com.silphengine.application.mappers.ExpansionMapper;
import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.dto.responses.ExpansionResponse;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.ExpansionService;
import com.silphengine.infrastructure.repositories.ExpansionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpansionServiceImpl implements ExpansionService {

    private final ExpansionRepository expansionRepository;
    private final ExpansionMapper expansionMapper;

    @Override
    @Transactional
    public ExpansionResponse createExpansion(ExpansionRequest expansionRequest) {

        expansionRepository.findByExternalId(expansionRequest.externalId())
                .ifPresent(e -> {
                    throw new DuplicateResourceException("Expansion already exists with ID: " + expansionRequest.externalId());
                });

        return expansionMapper.toResponse(expansionRepository.save(expansionMapper.toEntity(expansionRequest)));
    }

    @Override
    public ExpansionResponse getByExternalId(String externalId) {
        return expansionMapper.toResponse(expansionRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Expansion with ID " + externalId + " not found")));
    }

    @Override
    @Transactional
    public void removeByExternalId(String externalId) {

        Expansion expansion =  expansionRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Expansion with ID " + externalId + " not found"));

        expansionRepository.delete(expansion);
    }

    @Override
    @Transactional
    public ExpansionResponse updateByExternalId(ExpansionRequest expansionRequest) {

        Expansion expansion = expansionRepository.findByExternalId(expansionRequest.externalId())
                .orElseThrow(() -> new ResourceNotFoundException("Expansion with ID " + expansionRequest.externalId() + " not found"));

        expansionMapper.updateEntityFromRequest(expansion, expansionRequest);

        return expansionMapper.toResponse(expansionRepository.save(expansion));
    }
}
