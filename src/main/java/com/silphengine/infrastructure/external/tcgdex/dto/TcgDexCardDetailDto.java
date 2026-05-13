package com.silphengine.infrastructure.external.tcgdex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TcgDexCardDetailDto(
        String id,
        String localId,
        String name,
        String image,
        String category,
        String illustrator,
        String rarity,
        SetShortDto set,
        VariantsDto variants,
        @JsonProperty("variants_detailed") List<VariantDetailDto> variantsDetailed,
        List<Integer> dexId,
        Integer hp,
        List<String> types,
        String evolveFrom,
        String description,
        String stage,
        List<AttackDto> attacks,
        List<WeaknessDto> weaknesses,
        Integer retreat,
        String regulationMark,
        LegalDto legal,
        String updated,
        PricingDto pricing
) {
    public record SetShortDto(
            String id,
            String name,
            String logo,
            String symbol,
            CardCountShortDto cardCount
    ) {}

    public record CardCountShortDto(
            Integer official,
            Integer total
    ) {}

    public record VariantsDto(
            Boolean firstEdition,
            Boolean holo,
            Boolean normal,
            Boolean reverse,
            Boolean wPromo
    ) {}

    public record VariantDetailDto(
            String type,
            String size,
            String variantId
    ) {}

    public record AttackDto(
            List<String> cost,
            String name,
            String effect,
            Object damage
    ) {}

    public record WeaknessDto(
            String type,
            String value
    ) {}

    public record LegalDto(
            Boolean standard,
            Boolean expanded
    ) {}

    public record PricingDto(
            CardMarketDto cardmarket,
            Object tcgplayer
    ) {}

    public record CardMarketDto(
            String updated,
            String unit,
            Integer idProduct,
            Double avg,
            Double low,
            Double trend,
            @JsonProperty("avg-holo") Double avgHolo,
            @JsonProperty("low-holo") Double lowHolo,
            @JsonProperty("trend-holo") Double trendHolo
    ) {}
}
