package com.silphengine.infrastructure.external.tcgdex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TcgDexSetDetailDto(
        String id,
        String name,
        String logo,
        String symbol,
        @JsonProperty("releaseDate") String releaseDate,
        SerieDto serie,
        @JsonProperty("tcgOnline") String tcgOnline,
        AbbreviationDto abbreviation,
        LegalDto legal,
        CardCountDto cardCount,
        List<CardDto> cards
) {
    public record CardCountDto(
            int total,
            int official,
            int normal,
            int holo,
            int reverse,
            int firstEd
    ) {}
    public record CardDto(
            String id,
            String localId,
            String name,
            String image
    ) {}
    public record LegalDto(
            boolean standard,
            boolean expanded
    ) {}
    public record SerieDto(
            String id,
            String name
    ) {}
    public record AbbreviationDto(
            String official
    ) {}
}