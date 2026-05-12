package com.silphengine.infrastructure.external.tcgdex.dto;

public record TcgDexSetSummaryDto(
        String id,
        String name,
        String logo,
        String symbol,
        CardCountDto cardCount
) {
    public record CardCountDto(
            int total,
            int official
    ) {}
}
