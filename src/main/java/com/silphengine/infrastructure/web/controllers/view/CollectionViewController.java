package com.silphengine.infrastructure.web.controllers.view;

import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.dto.responses.InventoryCardResponse;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.services.InventoryCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CollectionViewController {

    private final InventoryCardService inventoryCardService;

    @GetMapping("/collection")
    public String showMyCollection(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal User user,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);

        Page<InventoryCardResponse> collectionPage = inventoryCardService.getUniqueCollectionCards(user.getId(), pageable);

        Page<CardResponse> cardPage = collectionPage.map(InventoryCardResponse::card);


        model.addAttribute("cards", cardPage);
        model.addAttribute("page", cardPage);
        model.addAttribute("totalItems", cardPage.getTotalElements());
        model.addAttribute("baseUrl", "/collection");
        model.addAttribute("expansionName", null);
        model.addAttribute("searchQuery", null);

        return "cards";
    }
}