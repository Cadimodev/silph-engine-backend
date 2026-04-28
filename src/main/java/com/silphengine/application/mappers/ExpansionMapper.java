package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.dto.responses.ExpansionResponse;
import com.silphengine.domain.entities.Expansion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper
public interface ExpansionMapper {

    @Mapping(target = "id", ignore = true)
    Expansion toEntity(ExpansionRequest request);

    ExpansionResponse toResponse(Expansion expansion);

    default void updateEntityFromRequest(Expansion expansion, ExpansionRequest expansionRequest) {

        if (expansion == null || expansionRequest == null) {
            return;
        }

        expansion.updateDetails(
                expansionRequest.name(),
                expansionRequest.serieName(),
                expansionRequest.releaseDate(),
                expansionRequest.totalCards(),
                expansionRequest.logoUrl()
        );
    }
}
