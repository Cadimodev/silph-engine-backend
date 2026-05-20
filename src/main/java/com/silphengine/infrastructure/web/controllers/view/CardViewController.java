package com.silphengine.infrastructure.web.controllers.view;

import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.services.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CardViewController {

    private final CardService cardService;

    @GetMapping("/cards")
    public String showCardCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);

        // 2. Consumimos tu servicio real que devuelve Page<CardResponse>
        Page<CardResponse> cardPage = cardService.getAllCards(pageable);

        model.addAttribute("cards", cardPage.getContent());
        model.addAttribute("currentPage", cardPage.getNumber());
        model.addAttribute("totalPages", cardPage.getTotalPages());
        model.addAttribute("totalItems", cardPage.getTotalElements());

        return "cards";
    }
}