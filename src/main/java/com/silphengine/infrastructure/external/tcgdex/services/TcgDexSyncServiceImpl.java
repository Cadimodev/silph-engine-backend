package com.silphengine.infrastructure.external.tcgdex.services;

import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.services.CardCatalogSyncService;
import com.silphengine.infrastructure.external.tcgdex.client.TcgDexClient;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexSetDetailDto;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexSetSummaryDto;
import com.silphengine.infrastructure.external.tcgdex.mappers.TcgDexExpansionMapper;
import com.silphengine.infrastructure.repositories.CardRepository;
import com.silphengine.infrastructure.repositories.ExpansionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TcgDexSyncServiceImpl implements CardCatalogSyncService {

    private final TcgDexClient tcgDexClient;
    private final TcgDexExpansionMapper tcgDexExpansionMapper;
    private final ExpansionRepository expansionRepository;
    private final CardRepository cardRepository;

    @Override
    public void syncAll() {
        syncExpansions();
    }

    private void syncExpansions() {

        List<TcgDexSetSummaryDto> externalSets = tcgDexClient.getSets();

        List<Expansion> existingExpansions = expansionRepository.findAll();
        Map<String, Expansion> expansionMap = existingExpansions.stream()
                .collect(Collectors.toMap(Expansion::getExternalId, e -> e));

        List<Expansion> entitiesToSave = new ArrayList<>();

        for (TcgDexSetSummaryDto setSummary : externalSets) {
            try {
                TcgDexSetDetailDto setDetail = tcgDexClient.getSetById(setSummary.id());
                Expansion existingExpansion = expansionMap.get(setSummary.id());

                if (existingExpansion != null) {
                    tcgDexExpansionMapper.updateFromDetailDto(setDetail, existingExpansion);
                    entitiesToSave.add(existingExpansion);
                } else {
                    Expansion newExpansion = tcgDexExpansionMapper.toEntity(setDetail);
                    entitiesToSave.add(newExpansion);
                }

                // pause to avoid rate limiting from TCGDex
                Thread.sleep(100);

            } catch (Exception e) {
                System.err.println("Error syncing expansion " + setSummary.id() + ": " + e.getMessage());
            }
        }

        expansionRepository.saveAll(entitiesToSave);
    }
}
