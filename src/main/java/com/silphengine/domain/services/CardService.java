package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.CardRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {

    CardResponse createCard(CardRequest cardRequest);

    CardResponse getByExternalId(String externalId);

    Page<CardResponse> getAllCards(Pageable pageable);

    Page<CardResponse> getByExternalExpansionId(String externalExpansionId, Pageable pageable);

    CardResponse updateByExternalId(String externalId, CardRequest cardRequest);

    void deleteByExternalId(String externalId);
}
