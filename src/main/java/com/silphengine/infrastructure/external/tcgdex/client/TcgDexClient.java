package com.silphengine.infrastructure.external.tcgdex.client;

import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexCardDetailDto;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexSetDetailDto;
import com.silphengine.infrastructure.external.tcgdex.dto.TcgDexSetSummaryDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface TcgDexClient {

    @GetExchange("sets")
    List<TcgDexSetSummaryDto> getSets();

    @GetExchange("sets/{setId}")
    TcgDexSetDetailDto getSetById(@PathVariable("setId") String setId);

    @GetExchange("cards/{cardId}")
    TcgDexCardDetailDto getCardById(@PathVariable("cardId") String cardId);
}
