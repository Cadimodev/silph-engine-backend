package com.silphengine.infrastructure.web.controllers.view;

import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.dto.responses.ExpansionResponse;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.services.CardService;
import com.silphengine.domain.services.ExpansionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CardViewController {

    private final CardService cardService;
    private final ExpansionService expansionService;

    @GetMapping("/cards")
    public String showCardCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CardResponse> cardPage;

        if (search != null && !search.isBlank()) {
            cardPage = cardService.getAllCards(pageable);
            model.addAttribute("searchQuery", search);
        } else {
            cardPage = cardService.getAllCards(pageable);
        }

        populateModel(model, cardPage, "/cards", null);

        return "cards";
    }

    @GetMapping("/expansions/{externalId}/cards")
    public String showCardsByExpansion(
            @PathVariable String externalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "externalId"));

        Page<CardResponse> cardPage = cardService.getByExternalExpansionId(externalId, pageable);

        ExpansionResponse expansion = expansionService.getByExternalId(externalId);

        populateModel(model, cardPage, "/expansions/" + externalId + "/cards", expansion.name());

        return "cards";
    }

    private void populateModel(Model model, Page<CardResponse> cardPage, String baseUrl, String expansionName) {
        model.addAttribute("cards", cardPage.getContent());
        model.addAttribute("page", cardPage);
        model.addAttribute("currentPage", cardPage.getNumber());
        model.addAttribute("totalPages", cardPage.getTotalPages());
        model.addAttribute("totalItems", cardPage.getTotalElements());
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("expansionName", expansionName);
    }
}