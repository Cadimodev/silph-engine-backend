package com.silphengine.infrastructure.external.tcgdex.services;

import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.services.CardCatalogSyncService;
import com.silphengine.infrastructure.external.tcgdex.client.TcgDexClient;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexCardDetailDto;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexSetDetailDto;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexSetSummaryDto;
import com.silphengine.infrastructure.external.tcgdex.mappers.TcgDexCardMapper;
import com.silphengine.infrastructure.external.tcgdex.mappers.TcgDexExpansionMapper;
import com.silphengine.infrastructure.repositories.CardRepository;
import com.silphengine.infrastructure.repositories.ExpansionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcgDexSyncServiceImpl implements CardCatalogSyncService {

    private final TcgDexClient tcgDexClient;
    private final TcgDexExpansionMapper tcgDexExpansionMapper;
    private final TcgDexCardMapper tcgDexCardMapper;
    private final ExpansionRepository expansionRepository;
    private final CardRepository cardRepository;

    @Override
    @Async
    public void syncAll() {
        log.info("Starting TCGDex Full Catalog Sync...");
        Map<Expansion, List<TcgDexSetDetailDto.CardDto>> expansionsWithCardsToSync = syncExpansions();
        syncCards(expansionsWithCardsToSync);
        log.info("TCGDex Full Catalog Sync completed successfully!");
    }

    private Map<Expansion, List<TcgDexSetDetailDto.CardDto>> syncExpansions() {

        List<TcgDexSetSummaryDto> externalSets = tcgDexClient.getSets();

        List<Expansion> existingExpansions = expansionRepository.findAll();
        Map<String, Expansion> expansionMap = existingExpansions.stream()
                .collect(Collectors.toMap(Expansion::getExternalId, e -> e));

        Map<String, Expansion> entitiesToSaveMap = new HashMap<>();
        Map<Expansion, List<TcgDexSetDetailDto.CardDto>> result = new HashMap<>();

        if (externalSets != null) {
            log.info("Found {} expansions to sync.", externalSets.size());
            int count = 0;
            for (TcgDexSetSummaryDto setSummary : externalSets) {
                count++;
                try {
                    TcgDexSetDetailDto setDetail = tcgDexClient.getSetById(setSummary.id());

                    if (setDetail != null && setDetail.serie() != null && "tcgp".equalsIgnoreCase(setDetail.serie().id())) {
                        log.warn("Skipping set '{}' ({}) because it belongs to the Pokémon TCG Pocket series (tcgp).",
                                setSummary.name(), setSummary.id());
                        continue;
                    }

                    Expansion existingExpansion = expansionMap.get(setSummary.id());

                    Expansion savedOrUpdatedExpansion;
                    if (existingExpansion != null) {
                        tcgDexExpansionMapper.updateFromDetailDto(setDetail, existingExpansion);
                        savedOrUpdatedExpansion = existingExpansion;
                    } else {
                        savedOrUpdatedExpansion = tcgDexExpansionMapper.toEntity(setDetail);
                        expansionMap.put(savedOrUpdatedExpansion.getExternalId(), savedOrUpdatedExpansion);
                    }

                    entitiesToSaveMap.put(savedOrUpdatedExpansion.getExternalId(), savedOrUpdatedExpansion);

                    // Keep the card summaries for the next step, linking them to the expansion entity
                    if (setDetail.cards() != null && !setDetail.cards().isEmpty()) {
                        result.put(savedOrUpdatedExpansion, setDetail.cards());
                    }

                    // Pause to avoid rate limiting from TCGDex
                    Thread.sleep(100);

                } catch (Exception e) {
                    log.error("Error syncing expansion {}: {}", setSummary.id(), e.getMessage());
                }
                
                if (count % 10 == 0) {
                    log.info("Processed {}/{} expansions...", count, externalSets.size());
                }
            }
        }

        expansionRepository.saveAll(entitiesToSaveMap.values());
        log.info("Finished syncing all expansions.");

        return result;
    }

    private void syncCards(Map<Expansion, List<TcgDexSetDetailDto.CardDto>> expansionsWithCardsToSync) {

        int totalExpansions = expansionsWithCardsToSync.size();
        int currentExpansionCount = 0;

        for (Map.Entry<Expansion, List<TcgDexSetDetailDto.CardDto>> entry : expansionsWithCardsToSync.entrySet()) {
            Expansion expansion = entry.getKey();
            List<TcgDexSetDetailDto.CardDto> cardsInSet = entry.getValue();
            currentExpansionCount++;

            if (cardsInSet != null) {
                log.info("Syncing {} cards for expansion '{}' ({}/{})", cardsInSet.size(), expansion.getName(), currentExpansionCount, totalExpansions);

                List<Card> existingCards = cardRepository.findByExpansion(expansion);
                Map<String, Card> cardMap = existingCards.stream()
                        .collect(Collectors.toMap(Card::getExternalId, e -> e));
                Map<String, Card> expansionEntitiesToSave = new HashMap<>();
                int cardCount = 0;

                for (TcgDexSetDetailDto.CardDto cardDto : cardsInSet) {
                    cardCount++;
                    try {
                        TcgDexCardDetailDto cardDetail = tcgDexClient.getCardById(cardDto.id());
                        Card existingCard = cardMap.get(cardDto.id());

                        if (existingCard != null) {
                            tcgDexCardMapper.updateFromDetailDto(cardDetail, existingCard);
                            expansionEntitiesToSave.put(existingCard.getExternalId(), existingCard);
                        } else {
                            Card newCard = tcgDexCardMapper.toEntity(cardDetail, expansion);
                            expansionEntitiesToSave.put(newCard.getExternalId(), newCard);

                            cardMap.put(newCard.getExternalId(), newCard);
                        }

                        // Pause to avoid rate limiting from TCGDex
                        Thread.sleep(100);
                    } catch (RestClientException rce) {
                         log.error("Error syncing card {}: HTTP Error - {}", cardDto.id(), rce.getMessage());
                         if(rce.getCause() != null) {
                             log.error("Cause: {}", rce.getCause().getMessage());
                         }
                    } catch (Exception e) {
                        log.error("Error syncing card {}: {}", cardDto.id(), e.getMessage());
                    }
                    
                    if (cardCount % 50 == 0) {
                        log.info("Processed {}/{} cards in '{}'...", cardCount, cardsInSet.size(), expansion.getName());
                    }
                }

                log.info("Saving {} cards for expansion '{}' to database...", expansionEntitiesToSave.size(), expansion.getName());
                try {
                    cardRepository.saveAll(expansionEntitiesToSave.values());
                } catch (Exception e) {
                    log.error("Failed to save expansion '{}' to database: {}", expansion.getName(), e.getMessage());
                }

            }
        }
    }
}