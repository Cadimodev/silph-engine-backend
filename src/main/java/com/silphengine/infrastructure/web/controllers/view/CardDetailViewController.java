package com.silphengine.infrastructure.web.controllers.view;

import com.silphengine.domain.dto.requests.InventoryCardRequest;
import com.silphengine.domain.dto.requests.UpdateInventoryCardRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.dto.responses.ExpansionResponse;
import com.silphengine.domain.dto.responses.InventoryCardResponse;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.CardCondition;
import com.silphengine.domain.enums.CardFinish;
import com.silphengine.domain.services.CardService;
import com.silphengine.domain.services.ExpansionService;
import com.silphengine.domain.services.InventoryCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CardDetailViewController {

    private final CardService cardService;
    private final ExpansionService expansionService;
    private final InventoryCardService inventoryCardService;

    @GetMapping("/cards/{externalId}")
    public String showCardDetail(@PathVariable String externalId,
                                 @AuthenticationPrincipal User user,
                                 Model model) {

        CardResponse card = cardService.getByExternalId(externalId);
        ExpansionResponse expansion = expansionService.getByExternalId(card.expansionExternalId());

        List<InventoryCardResponse> inventoryEntries = inventoryCardService.getInventoryCardsByCardId(card.id(), user.getId());

        int totalQuantity = inventoryEntries.stream()
                .mapToInt(InventoryCardResponse::quantity)
                .sum();

        model.addAttribute("card", card);
        model.addAttribute("expansionName", expansion.name());
        model.addAttribute("quantity", totalQuantity);
        model.addAttribute("inventory", inventoryEntries);
        model.addAttribute("conditions", CardCondition.values());
        model.addAttribute("finishes", CardFinish.values());

        return "card-detail";
    }

    @PostMapping("/cards/collection")
    @ResponseBody
    public ResponseEntity<InventoryCardResponse> addCardWeb(
            @Valid @RequestBody InventoryCardRequest request,
            @AuthenticationPrincipal User user) {

        InventoryCardResponse response = inventoryCardService.createInventoryCard(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/cards/collection/{inventoryCardId}/quantity")
    @ResponseBody
    public ResponseEntity<InventoryCardResponse> updateQuantityWeb(
            @PathVariable UUID inventoryCardId,
            @RequestParam int change,
            @AuthenticationPrincipal User user) {

        var currentItem = inventoryCardService.getInventoryCard(inventoryCardId, user.getId());
        int newQuantity = currentItem.quantity() + change;

        if (newQuantity <= 0) {
            inventoryCardService.deleteInventoryCard(inventoryCardId, user.getId());
            return ResponseEntity.noContent().build();
        }

        UpdateInventoryCardRequest updateRequest = new UpdateInventoryCardRequest(newQuantity);

        InventoryCardResponse response = inventoryCardService.updateInventoryCard(inventoryCardId, updateRequest, user.getId());
        return ResponseEntity.ok(response);
    }
}