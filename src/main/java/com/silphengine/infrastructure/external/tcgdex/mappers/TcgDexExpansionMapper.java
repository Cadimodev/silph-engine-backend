package com.silphengine.infrastructure.external.tcgdex.mappers;

import com.silphengine.domain.entities.Expansion;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexSetDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDate;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TcgDexExpansionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "id", target = "externalId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "logo", target = "logoUrl")
    @Mapping(source = "cardCount.total", target = "totalCards")
    @Mapping(source = "serie.name", target = "serieName")
    @Mapping(source = "releaseDate", target = "releaseDate")
    Expansion toEntity(TcgDexSetDetailDto detailDto);

    default void updateFromDetailDto(TcgDexSetDetailDto detailDto, @MappingTarget Expansion expansion) {
        if (detailDto == null) {
            return;
        }

        LocalDate parsedDate = null;
        if (detailDto.releaseDate() != null) {
            try {
                parsedDate = LocalDate.parse(detailDto.releaseDate());
            } catch (Exception e) {
                // If it fails to parse, leave it as null or handle it accordingly
            }
        }

        String serieName = detailDto.serie() != null ? detailDto.serie().name() : null;
        int totalCards = detailDto.cardCount() != null ? detailDto.cardCount().total() : 0;

        expansion.updateDetails(
                detailDto.name(),
                serieName,
                parsedDate,
                totalCards,
                detailDto.logo()
        );
    }
}
