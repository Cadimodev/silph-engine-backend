package com.silphengine.infrastructure.external.tcgdex.services;

import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.infrastructure.external.tcgdex.client.TcgDexClient;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexCardDetailDto;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexSetDetailDto;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexSetSummaryDto;
import com.silphengine.infrastructure.external.tcgdex.mappers.TcgDexCardMapper;
import com.silphengine.infrastructure.external.tcgdex.mappers.TcgDexExpansionMapper;
import com.silphengine.infrastructure.repositories.CardRepository;
import com.silphengine.infrastructure.repositories.ExpansionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TcgDexSyncServiceImplTest {

    @Mock
    private TcgDexClient tcgDexClient;
    @Mock
    private TcgDexExpansionMapper tcgDexExpansionMapper;
    @Mock
    private TcgDexCardMapper tcgDexCardMapper;
    @Mock
    private ExpansionRepository expansionRepository;
    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TcgDexSyncServiceImpl tcgDexSyncService;

    @BeforeEach
    void setUp() {
        reset(tcgDexClient, tcgDexExpansionMapper, tcgDexCardMapper, expansionRepository, cardRepository);
    }

    @Test
    void syncAll_ShouldSyncExpansionsAndCardsSuccessfully() {

        // Given
        TcgDexSetSummaryDto summaryDto = new TcgDexSetSummaryDto("base1", "Base Set", "logoUrl", "symbolUrl", new TcgDexSetSummaryDto.CardCountDto(102, 102));
        when(tcgDexClient.getSets()).thenReturn(List.of(summaryDto));
        when(expansionRepository.findAll()).thenReturn(Collections.emptyList());

        TcgDexSetDetailDto.CardDto cardDto = new TcgDexSetDetailDto.CardDto("base1-1", "1", "Alakazam", "imageUrl");
        TcgDexSetDetailDto detailDto = new TcgDexSetDetailDto("base1", "Base Set", "logoUrl", "symbolUrl", "1999-01-09", null, null, null, null, null, List.of(cardDto));
        when(tcgDexClient.getSetById("base1")).thenReturn(detailDto);

        Expansion mockExpansion = mock(Expansion.class);
        when(mockExpansion.getExternalId()).thenReturn("base1");
        when(tcgDexExpansionMapper.toEntity(detailDto)).thenReturn(mockExpansion);

        // We don't stub expansionRepository.saveAll(List.of(mockExpansion)) strictly, we use anyCollection()
        when(expansionRepository.saveAll(anyCollection())).thenReturn(List.of(mockExpansion));

        when(cardRepository.findByExpansion(any())).thenReturn(Collections.emptyList());

        TcgDexCardDetailDto cardDetailDto = new TcgDexCardDetailDto("base1-1", "1", "Alakazam", "imageUrl", "Pokemon", null, "Rare", null, null, null, null, 100, null, null, null, null, null, null, 3, null, null, null, null);
        when(tcgDexClient.getCardById("base1-1")).thenReturn(cardDetailDto);

        Card mockCard = mock(Card.class);
        when(mockCard.getExternalId()).thenReturn("base1-1");
        when(tcgDexCardMapper.toEntity(cardDetailDto, mockExpansion)).thenReturn(mockCard);

        // When
        tcgDexSyncService.syncAll();

        // Then
        verify(tcgDexClient, times(1)).getSets();
        verify(tcgDexClient, times(1)).getSetById("base1");
        verify(expansionRepository, times(1)).saveAll(anyCollection());
        verify(tcgDexClient, times(1)).getCardById("base1-1");
        verify(cardRepository, times(1)).saveAll(anyCollection());
        verify(tcgDexExpansionMapper, never()).updateFromDetailDto(any(), any());
        verify(tcgDexCardMapper, never()).updateFromDetailDto(any(), any());
    }

    @Test
    void syncAll_ShouldUpdateExistingEntities_WhenAlreadyInDatabase() {

        // Given
        TcgDexSetSummaryDto summaryDto = new TcgDexSetSummaryDto("base1", "Base Set", "logoUrl", null, null);
        when(tcgDexClient.getSets()).thenReturn(List.of(summaryDto));

        Expansion existingExpansion = mock(Expansion.class);
        when(existingExpansion.getExternalId()).thenReturn("base1");
        when(expansionRepository.findAll()).thenReturn(List.of(existingExpansion));

        TcgDexSetDetailDto.CardDto cardDto = new TcgDexSetDetailDto.CardDto("base1-1", "1", "Alakazam", "imageUrl");
        TcgDexSetDetailDto detailDto = new TcgDexSetDetailDto("base1", "Base Set", "logoUrl", null, null, null, null, null, null, null, List.of(cardDto));
        when(tcgDexClient.getSetById("base1")).thenReturn(detailDto);
        
        when(expansionRepository.saveAll(anyCollection())).thenReturn(List.of(existingExpansion));

        Card existingCard = mock(Card.class);
        when(existingCard.getExternalId()).thenReturn("base1-1");
        when(cardRepository.findByExpansion(any())).thenReturn(List.of(existingCard));

        TcgDexCardDetailDto cardDetailDto = new TcgDexCardDetailDto("base1-1", "1", "Alakazam Updated", "newImageUrl", "Pokemon", null, "Rare", null, null, null, null, 100, null, null, null, null, null, null, 3, null, null, null, null);
        when(tcgDexClient.getCardById("base1-1")).thenReturn(cardDetailDto);

        // When
        tcgDexSyncService.syncAll();

        // Then
        verify(tcgDexExpansionMapper, times(1)).updateFromDetailDto(detailDto, existingExpansion);
        verify(tcgDexExpansionMapper, never()).toEntity(any());
        verify(tcgDexCardMapper, times(1)).updateFromDetailDto(cardDetailDto, existingCard);
        verify(tcgDexCardMapper, never()).toEntity(any(), any());
        verify(expansionRepository, times(1)).saveAll(anyCollection());
        verify(cardRepository, times(1)).saveAll(anyCollection());
    }

    @Test
    void syncAll_ShouldContinueSyncing_WhenOneCardFails() {

        // Given
         TcgDexSetSummaryDto summaryDto = new TcgDexSetSummaryDto("base1", "Base Set", "logoUrl", null, null);
         when(tcgDexClient.getSets()).thenReturn(List.of(summaryDto));
         when(expansionRepository.findAll()).thenReturn(Collections.emptyList());

         TcgDexSetDetailDto.CardDto cardDto1 = new TcgDexSetDetailDto.CardDto("base1-1", "1", "Alakazam", "imageUrl");
         TcgDexSetDetailDto.CardDto cardDto2 = new TcgDexSetDetailDto.CardDto("base1-2", "2", "Blastoise", "imageUrl");
         TcgDexSetDetailDto detailDto = new TcgDexSetDetailDto("base1", "Base Set", "logoUrl", null, null, null, null, null, null, null, List.of(cardDto1, cardDto2));
         when(tcgDexClient.getSetById("base1")).thenReturn(detailDto);
         
         Expansion mockExpansion = mock(Expansion.class);
         when(mockExpansion.getExternalId()).thenReturn("base1");
         when(tcgDexExpansionMapper.toEntity(detailDto)).thenReturn(mockExpansion);
         when(expansionRepository.saveAll(anyCollection())).thenReturn(List.of(mockExpansion));
         
         when(cardRepository.findByExpansion(any())).thenReturn(Collections.emptyList());

         when(tcgDexClient.getCardById("base1-1")).thenThrow(new RuntimeException("API Timeout for card 1"));

         TcgDexCardDetailDto cardDetailDto2 = new TcgDexCardDetailDto("base1-2", "2", "Blastoise", "imageUrl", "Pokemon", null, "Rare", null, null, null, null, 100, null, null, null, null, null, null, 3, null, null, null, null);
         when(tcgDexClient.getCardById("base1-2")).thenReturn(cardDetailDto2);
         Card mockCard2 = mock(Card.class);
         when(mockCard2.getExternalId()).thenReturn("base1-2");
         when(tcgDexCardMapper.toEntity(cardDetailDto2, mockExpansion)).thenReturn(mockCard2);

         // When
         tcgDexSyncService.syncAll();

         // Then
         verify(tcgDexClient, times(1)).getCardById("base1-1");
         verify(tcgDexClient, times(1)).getCardById("base1-2");
         verify(cardRepository, times(1)).saveAll(anyCollection());
    }
}