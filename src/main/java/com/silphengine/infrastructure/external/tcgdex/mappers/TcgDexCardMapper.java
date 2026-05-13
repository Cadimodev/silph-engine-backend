package com.silphengine.infrastructure.external.tcgdex.mappers;

import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.BadRequestException;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexCardDetailDto;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TcgDexCardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", source = "detailDto.id")
    @Mapping(target = "name", source = "detailDto.name")
    @Mapping(target = "imageUrl", source = "detailDto.image")
    @Mapping(target = "rarity", source = "detailDto.rarity")
    @Mapping(target = "expansion", source = "expansion")
    @Mapping(target = "regulationMark", source = "detailDto.regulationMark")
    @Mapping(target = "cardCategory", source = "detailDto.category", qualifiedByName = "stringToCategory")
    @Mapping(target = "types", source = "detailDto.types", qualifiedByName = "stringsToTypes")
    Card toEntity(TcgDexCardDetailDto detailDto, Expansion expansion);

    default void updateFromDetailDto(TcgDexCardDetailDto detailDto, @MappingTarget Card card) {

        if (detailDto == null || card == null) {
            return;
        }

        card.updateDetails(
                detailDto.name(),
                detailDto.rarity(),
                mapStringToCardCategory(detailDto.category()),
                mapStringsToTypes(detailDto.types()),
                detailDto.image(),
                detailDto.regulationMark()
        );
    }

    @Named("stringToCategory")
    default CardCategory mapStringToCardCategory(String cardCategoryString) {

        if (cardCategoryString == null || cardCategoryString.isBlank()) {
            throw new BadRequestException("Card category cannot be null or empty");
        }
        try {
            return CardCategory.valueOf(cardCategoryString.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Category not recognized: " + cardCategoryString);
        }
    }

    @Named("stringsToTypes")
    default List<CardType> mapStringsToTypes(List<String> typeStrings) {

        if (typeStrings == null || typeStrings.isEmpty()) {
            return new ArrayList<>();
        }
        return typeStrings.stream()
                .map(type -> {
                    try {
                        return CardType.valueOf(type.toUpperCase().trim());
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Card type not recognized: " + type);
                    }
                })
                .collect(Collectors.toList());
    }
}
