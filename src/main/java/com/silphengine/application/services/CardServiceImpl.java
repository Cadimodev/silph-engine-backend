package com.silphengine.application.services;

import com.silphengine.application.mappers.CardMapper;
import com.silphengine.domain.dto.requests.CardRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.CardService;
import com.silphengine.infrastructure.repositories.CardRepository;
import com.silphengine.infrastructure.repositories.ExpansionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {

    private final CardMapper cardMapper;

    private final CardRepository cardRepository;

    private final ExpansionRepository expansionRepository;

    @Override
    @Transactional
    public CardResponse createCard(CardRequest cardRequest) {

        Expansion expansion = expansionRepository.findByExternalId(cardRequest.expansionExternalId()).orElseThrow(
                () -> new ResourceNotFoundException("Expansion with ID " + cardRequest.expansionExternalId() + " not found")
        );

        cardRepository.findByExternalId(cardRequest.externalId()) .ifPresent(e -> {
            throw new DuplicateResourceException("Card already exists with ID: " + cardRequest.externalId());
        });

        return cardMapper.toResponse(cardRepository.save(cardMapper.toEntity(cardRequest, expansion)));
    }

    @Override
    public CardResponse getByExternalId(String externalId) {

        return cardMapper.toResponse(cardRepository.findByExternalId(externalId).orElseThrow(
                () -> new ResourceNotFoundException("Card with ID: " + externalId + " not found")
        ));
    }

    @Override
    public List<CardResponse> getByExternalExpansionId(String externalExpansionId) {

        return cardRepository.findByExpansion_ExternalId(externalExpansionId)
                .stream()
                .map(cardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CardResponse updateByExternalId(CardRequest cardRequest) {

        Card card = cardRepository.findByExternalId(cardRequest.externalId())
                .orElseThrow(() -> new ResourceNotFoundException("Card with ID: " + cardRequest.externalId() + " not found"));

        if (!card.getExpansion().getExternalId().equals(cardRequest.expansionExternalId())) {
            Expansion newExpansion = expansionRepository.findByExternalId(cardRequest.expansionExternalId())
                    .orElseThrow(() -> new ResourceNotFoundException("New Expansion not found with ID: " + cardRequest.expansionExternalId()));
            card.setExpansion(newExpansion);
        }

        cardMapper.updateEntityFromRequest(card, cardRequest);

        return cardMapper.toResponse(cardRepository.save(card));
    }

    @Override
    @Transactional
    public void deleteByExternalId(String externalId) {

        Card card = cardRepository.findByExternalId(externalId).orElseThrow(
                () -> new ResourceNotFoundException("Card with ID: " + externalId + " not found")
        );

        cardRepository.delete(card);

    }
}
