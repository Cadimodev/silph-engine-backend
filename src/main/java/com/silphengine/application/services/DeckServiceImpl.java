package com.silphengine.application.services;

import com.silphengine.application.config.FormatProperties;
import com.silphengine.application.mappers.DeckMapper;
import com.silphengine.domain.dto.requests.DeckCardRequest;
import com.silphengine.domain.dto.requests.DeckRequest;
import com.silphengine.domain.dto.responses.DeckResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Deck;
import com.silphengine.domain.entities.DeckCard;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.DeckService;
import com.silphengine.infrastructure.repositories.CardRepository;
import com.silphengine.infrastructure.repositories.DeckRepository;
import com.silphengine.infrastructure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeckServiceImpl implements DeckService {

    private final DeckMapper deckMapper;

    private final DeckRepository deckRepository;

    private final UserRepository userRepository;

    private final CardRepository cardRepository;

    private final FormatProperties formatProperties;

    @Override
    @Transactional
    public DeckResponse createDeck(DeckRequest deckRequest) {

        deckRepository.findByOwnerIdAndName(deckRequest.userId(), deckRequest.name()).ifPresent(
                d -> { throw new DuplicateResourceException("Deck already exists with that name for this user"); }
        );
        
        User user = userRepository.findById(deckRequest.userId()).orElseThrow(
                () -> new ResourceNotFoundException("User with ID: " + deckRequest.userId() + " not found")
        );

        List<DeckCard> deckCards = getDeckCardFromRequest(deckRequest.cards());
        
        Deck newDeck = deckMapper.toEntity(deckRequest, user, deckCards);
        
        newDeck.evaluateLegality(formatProperties.getStandardValidMarks());
        
        return deckMapper.toResponse(deckRepository.save(newDeck));
    }

    @Override
    public List<DeckResponse> getByOwnerId(UUID ownerId) {

        // TODO: Add pagination to this
        return deckRepository.findByOwnerId(ownerId)
                .stream()
                .map(deckMapper::toResponse)
                .toList();
    }

    @Override
    public DeckResponse getByOwnerIdAndDeckName(UUID ownerId, String deckName) {

        Deck deck = deckRepository.findByOwnerIdAndName(ownerId, deckName).orElseThrow(
                () -> new ResourceNotFoundException("Deck not found. Name: " + deckName + " - UserID: " + ownerId ));

        return deckMapper.toResponse(deck);

    }

    @Override
    @Transactional
    public DeckResponse updateDeck(UUID deckId, DeckRequest deckRequest) {

        Deck deck = deckRepository.findById(deckId).orElseThrow(
                () -> new ResourceNotFoundException("Deck with ID: " + deckId + " not found")
        );

        if (!deck.getName().equalsIgnoreCase(deckRequest.name())) {
            deckRepository.findByOwnerIdAndName(deckRequest.userId(), deckRequest.name()).ifPresent(
                    d -> { throw new DuplicateResourceException("Deck already exists with the name: " + deckRequest.name()); }
            );
        }

        List<DeckCard> deckCards = getDeckCardFromRequest(deckRequest.cards());

        deckMapper.updateEntityFromRequest(deck, deckRequest, deckCards);

        deck.evaluateLegality(formatProperties.getStandardValidMarks());

        return deckMapper.toResponse(deckRepository.save(deck));
    }

    @Override
    @Transactional
    public void deleteDeck(UUID deckId) {

        Deck deck = deckRepository.findById(deckId).orElseThrow(
                () -> new ResourceNotFoundException("Deck with ID: " + deckId + " not found")
        );

        deckRepository.delete(deck);
    }

    private List<DeckCard> getDeckCardFromRequest(List<DeckCardRequest> requestCards) {

        return requestCards.stream().map(cardReq -> {
            Card cardEntity = cardRepository.findById(cardReq.cardId()).orElseThrow(
                    () -> new ResourceNotFoundException("Card with ID: " + cardReq.cardId() + " not found")
            );

            return DeckCard.builder()
                    .card(cardEntity)
                    .quantity(cardReq.quantity())
                    .build();
        }).toList();
    }
}
