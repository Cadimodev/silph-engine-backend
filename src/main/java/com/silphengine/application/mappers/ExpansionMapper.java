package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.dto.responses.ExpansionResponse;
import com.silphengine.domain.entities.Expansion;
import org.springframework.stereotype.Component;

@Component
public class ExpansionMapper {

    public Expansion toEntity(ExpansionRequest request) {
        return Expansion.builder()
                .externalId(request.externalId())
                .name(request.name())
                .serieName(request.serieName())
                .releaseDate(request.releaseDate())
                .totalCards(request.totalCards())
                .logoUrl(request.logoUrl())
                .build();
    }

    public ExpansionResponse toResponse(Expansion expansion) {
        return new ExpansionResponse(
                expansion.getExternalId(),
                expansion.getName(),
                expansion.getSerieName(),
                expansion.getReleaseDate(),
                expansion.getTotalCards(),
                expansion.getLogoUrl()
        );
    }

    public void updateEntityFromRequest(Expansion expansion, ExpansionRequest expansionRequest) {
        expansion.updateDetails(
                expansionRequest.name(),
                expansionRequest.serieName(),
                expansionRequest.releaseDate(),
                expansionRequest.totalCards(),
                expansionRequest.logoUrl()
        );
    }
}
