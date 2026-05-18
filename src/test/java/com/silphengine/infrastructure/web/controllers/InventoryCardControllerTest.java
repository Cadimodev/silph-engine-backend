package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.dto.requests.InventoryCardRequest;
import com.silphengine.domain.dto.requests.UpdateInventoryCardRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.dto.responses.InventoryCardResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardCondition;
import com.silphengine.domain.enums.CardFinish;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.InventoryCardService;
import com.silphengine.infrastructure.web.config.TestSecurityConfig;
import com.silphengine.security.JwtService;
import com.silphengine.security.annotations.WithMockCustomUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(InventoryCardController.class)
@Import(TestSecurityConfig.class)
public class InventoryCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    JsonMapper objectMapper;

    @MockitoBean
    private InventoryCardService inventoryCardService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockCustomUser
    void createInventoryCard_shouldReturnCreatedAndInventoryCardResponse_whenRequestIsValid() throws Exception{

        // Given
        UUID cardId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        UUID inventoryCardId = UUID.randomUUID();
        InventoryCardRequest request = new InventoryCardRequest(cardId, 1, "Near Mint", "Normal");
        InventoryCardResponse response = new InventoryCardResponse(inventoryCardId, getDefaultCardResponse(getDefaultCard()), 1, CardCondition.NEAR_MINT, CardFinish.NORMAL);

        when(inventoryCardService.createInventoryCard(any(InventoryCardRequest.class), eq(ownerId))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/collection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(inventoryCardId.toString()))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    @WithMockCustomUser
    void createInventoryCard_shouldThrowBadRequest_whenRequestIsInvalid() throws Exception{

        // Given
        InventoryCardRequest request = new InventoryCardRequest(null, 1, "Near Mint", "Normal");

        // When & Then
        mockMvc.perform(post("/api/v1/collection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    void createInventoryCard_shouldThrowResourceNotFound_whenCardDoesNotExists() throws Exception{

        // Given
        UUID cardId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        InventoryCardRequest request = new InventoryCardRequest(cardId, 1, "Near Mint", "Normal");

        when(inventoryCardService.createInventoryCard(any(InventoryCardRequest.class), eq(ownerId))).thenThrow(new ResourceNotFoundException("Card with ID: " + cardId + " not found"));

        // When & Then
        mockMvc.perform(post("/api/v1/collection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void getCollection_shouldReturnOkAndPageOfInventoryCardResponse_whenRequestIsValid() throws Exception{

        // Given
        UUID ownerId = getAuthenticatedUserId();
        UUID inventoryCardId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        List<InventoryCardResponse> inventoryCardResponseList = new ArrayList<>();
        inventoryCardResponseList.add(new InventoryCardResponse(inventoryCardId, getDefaultCardResponse(getDefaultCard()), 1, CardCondition.NEAR_MINT, CardFinish.NORMAL));
        Page<InventoryCardResponse> response = new PageImpl<>(inventoryCardResponseList, pageable, inventoryCardResponseList.size());

        when(inventoryCardService.getCollection(eq(ownerId), any(Pageable.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/collection")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockCustomUser
    void getInventoryCard_shouldReturnOkAndInventoryCardResponse_whenCardExists() throws Exception{

        // Given
        UUID ownerId = getAuthenticatedUserId();
        UUID inventoryCardId = UUID.randomUUID();
        InventoryCardResponse response = new InventoryCardResponse(inventoryCardId, getDefaultCardResponse(getDefaultCard()), 1, CardCondition.NEAR_MINT, CardFinish.NORMAL);

        when(inventoryCardService.getInventoryCard(eq(inventoryCardId), eq(ownerId))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/collection/{inventoryCardId}", inventoryCardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(inventoryCardId.toString()))
                .andExpect(jsonPath("$.quantity").value(1));

    }

    @Test
    @WithMockCustomUser
    void getInventoryCard_shouldThrowResourceNotFound_whenCardDoesNotExists() throws Exception{

        // Given
        UUID ownerId = getAuthenticatedUserId();
        UUID inventoryCardId = UUID.randomUUID();

        when(inventoryCardService.getInventoryCard(eq(inventoryCardId), eq(ownerId))).thenThrow(new ResourceNotFoundException("Inventory Card with ID: " + inventoryCardId + " not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/collection/{inventoryCardId}", inventoryCardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockCustomUser
    void getInventoryCardsByCardId_shouldReturnOkAndListOfInventoryCardResponse_whenRequestIsValid() throws Exception{

        // Given
        UUID cardId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        UUID inventoryCardId = UUID.randomUUID();
        InventoryCardResponse response = new InventoryCardResponse(inventoryCardId, getDefaultCardResponse(getDefaultCard()), 1, CardCondition.NEAR_MINT, CardFinish.NORMAL);

        when(inventoryCardService.getInventoryCardsByCardId(eq(cardId), eq(ownerId))).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(get("/api/v1/collection/cards/{cardId}", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockCustomUser
    void updateInventoryCard_shouldReturnOkAndInventoryCardResponse_whenRequestIsValid() throws Exception{

        // Given
        UUID ownerId = getAuthenticatedUserId();
        UUID inventoryCardId = UUID.randomUUID();
        UpdateInventoryCardRequest request = new UpdateInventoryCardRequest(2);
        InventoryCardResponse response = new InventoryCardResponse(inventoryCardId, getDefaultCardResponse(getDefaultCard()), 2, CardCondition.NEAR_MINT, CardFinish.NORMAL);

        when(inventoryCardService.updateInventoryCard(eq(inventoryCardId), any(UpdateInventoryCardRequest.class), eq(ownerId))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/v1/collection/{inventoryCardId}", inventoryCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(inventoryCardId.toString()))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    @WithMockCustomUser
    void updateInventoryCard_shouldThrowBadRequest_whenRequestIsInvalid() throws Exception{

        // Given
        UUID inventoryCardId = UUID.randomUUID();
        // quantity must be positive, so 0 or negative will trigger @Valid to fail
        UpdateInventoryCardRequest request = new UpdateInventoryCardRequest(0);

        // When & Then
        mockMvc.perform(patch("/api/v1/collection/{inventoryCardId}", inventoryCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    void updateInventoryCard_shouldThrowResourceNotFound_whenCardDoesNotExists() throws Exception{

        // Given
        UUID ownerId = getAuthenticatedUserId();
        UUID inventoryCardId = UUID.randomUUID();
        UpdateInventoryCardRequest request = new UpdateInventoryCardRequest(2);

        when(inventoryCardService.updateInventoryCard(eq(inventoryCardId), any(UpdateInventoryCardRequest.class), eq(ownerId))).thenThrow(new ResourceNotFoundException("Inventory Card with ID: " + inventoryCardId + " not found"));

        // When & Then
        mockMvc.perform(patch("/api/v1/collection/{inventoryCardId}", inventoryCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void updateInventoryCard_shouldThrowForbidden_whenUserIsNotAuthorized() throws Exception{

        // Given
        UUID ownerId = getAuthenticatedUserId();
        UUID inventoryCardId = UUID.randomUUID();
        UpdateInventoryCardRequest request = new UpdateInventoryCardRequest(2);

        when(inventoryCardService.updateInventoryCard(eq(inventoryCardId), any(UpdateInventoryCardRequest.class), eq(ownerId)))
                .thenThrow(new AuthorizationDeniedException("You do not have permission to access this resource."));

        // When & Then
        mockMvc.perform(patch("/api/v1/collection/{inventoryCardId}", inventoryCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void deleteInventoryCard_shouldReturnNoContent_whenRequestIsValid() throws Exception{

        // Given
        UUID ownerId = getAuthenticatedUserId();
        UUID inventoryCardId = UUID.randomUUID();

        doNothing().when(inventoryCardService).deleteInventoryCard(inventoryCardId, ownerId);

        // When & Then
        mockMvc.perform(delete("/api/v1/collection/{inventoryCardId}", inventoryCardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser
    void deleteInventoryCard_shouldThrowResourceNotFound_whenCardDoesNotExists() throws Exception{

        // Given
        UUID ownerId = getAuthenticatedUserId();
        UUID inventoryCardId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("Inventory Card with ID: " + inventoryCardId + " not found")).when(inventoryCardService).deleteInventoryCard(inventoryCardId, ownerId);

        // When & Then
        mockMvc.perform(delete("/api/v1/collection/{inventoryCardId}", inventoryCardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void deleteInventoryCard_shouldThrowForbidden_whenUserIsNotAuthorized() throws Exception{

        // Given
        UUID ownerId = getAuthenticatedUserId();
        UUID inventoryCardId = UUID.randomUUID();

        doThrow(new AuthorizationDeniedException("You do not have permission to access this resource."))
                .when(inventoryCardService).deleteInventoryCard(inventoryCardId, ownerId);

        // When & Then
        mockMvc.perform(delete("/api/v1/collection/{inventoryCardId}", inventoryCardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    private Card getDefaultCard() {

        return Card.builder()
                .id(UUID.randomUUID())
                .externalId("sv02-203")
                .name("Magikarp")
                .rarity("Illustration rare")
                .cardCategory(CardCategory.POKEMON)
                .types(List.of(CardType.WATER))
                .imageUrl("https://assets.tcgdex.net/en/sv/sv02/203")
                .regulationMark("G")
                .build();
    }

    private CardResponse getDefaultCardResponse(Card card) {
        return new CardResponse(card.getExternalId(),
                card.getName(),
                card.getRarity(),
                card.getCardCategory(),
                card.getTypes(),
                card.getImageUrl(),
                null,
                card.getRegulationMark());
    }

    private UUID getAuthenticatedUserId() {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Objects.requireNonNull(user);
        return user.getId();
    }
}
