package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.dto.requests.DeckCardRequest;
import com.silphengine.domain.dto.requests.DeckRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.dto.responses.DeckCardResponse;
import com.silphengine.domain.dto.responses.DeckResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Deck;
import com.silphengine.domain.entities.DeckCard;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.DeckService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeckController.class)
@Import(TestSecurityConfig.class)

public class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    JsonMapper objectMapper;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private JwtService jwtService;


    @Test
    @WithMockCustomUser
    void createDeck_shouldReturnCreatedAndDeckResponse_whenRequestIsValid() throws Exception{

        // Given
        UUID cardId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        String deckName = "Default Deck";
        DeckCardResponse deckCardResponse = new DeckCardResponse(getDefaultCardResponse(getDefaultCard()), 4);

        DeckRequest request = new DeckRequest(deckName, List.of(new DeckCardRequest(cardId, 4)));
        DeckResponse response = new DeckResponse(deckId, ownerId, deckName, false, List.of(deckCardResponse));

        when(deckService.createDeck(any(DeckRequest.class), eq(ownerId))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(deckId.toString()))
                .andExpect(jsonPath("$.name").value(deckName))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()));

    }

    @Test
    @WithMockCustomUser
    void createDeck_shouldThrowBadRequest_whenRequestIsNotValid() throws Exception{

        // Given
        UUID cardId = UUID.randomUUID();
        String deckName = "";

        DeckRequest request = new DeckRequest(deckName, List.of(new DeckCardRequest(cardId, 4)));

        // When & Then
        mockMvc.perform(post("/api/v1/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockCustomUser
    void createDeck_shouldThrowDuplicateResource_whenDeckNameAlreadyExists() throws Exception{

        // Given
        UUID cardId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        String deckName = "Existing Deck";

        DeckRequest request = new DeckRequest(deckName, List.of(new DeckCardRequest(cardId, 4)));

        when(deckService.createDeck(any(DeckRequest.class), eq(ownerId))).thenThrow(new DuplicateResourceException("Deck already exists with that name for this user"));

        // When & Then
        mockMvc.perform(post("/api/v1/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockCustomUser
    void createDeck_shouldThrowResourceNotFound_whenCardDoesNotExists() throws Exception{

        // Given
        UUID cardId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        String deckName = "Default Deck";

        DeckRequest request = new DeckRequest(deckName, List.of(new DeckCardRequest(cardId, 4)));

        when(deckService.createDeck(any(DeckRequest.class), eq(ownerId))).thenThrow(new ResourceNotFoundException("Card with ID: " + cardId + " not found"));

        // When & Then
        mockMvc.perform(post("/api/v1/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void getDeckById_shouldReturnOkAndDeckResponse_whenRequestIsValid() throws Exception{

        // Given
        UUID deckId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        String deckName = "Default Deck";
        DeckCardResponse deckCardResponse = new DeckCardResponse(getDefaultCardResponse(getDefaultCard()), 4);

        DeckResponse response = new DeckResponse(deckId, ownerId, deckName, false, List.of(deckCardResponse));

        when(deckService.getByIdAndOwnerID(eq(deckId), eq(ownerId))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/decks/{deckId}", deckId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(deckId.toString()))
                .andExpect(jsonPath("$.name").value(deckName))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()));

    }

    @Test
    @WithMockCustomUser
    void getDeckById_shouldThrowResourceNotFoundException_whenDeckDoesNotExists() throws Exception{

        // Given
        UUID deckId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();

        when(deckService.getByIdAndOwnerID(eq(deckId), eq(ownerId))).thenThrow(new ResourceNotFoundException("Deck with ID: " + deckId + " not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/decks/{deckId}", deckId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockCustomUser
    void getMyDecks_shouldReturnAllDecks_whenDeckNameIsEmpty() throws Exception{

        // Given
        UUID deckId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        String deckName = "Default Deck";
        String deckName2 = "Second Deck";
        DeckCardResponse deckCardResponse = new DeckCardResponse(getDefaultCardResponse(getDefaultCard()), 4);

        Pageable pageable = PageRequest.of(0, 10);
        List<DeckResponse> deckResponseList = new ArrayList<>();
        deckResponseList.add(new DeckResponse(deckId, ownerId, deckName, false, List.of(deckCardResponse)));
        deckResponseList.add(new DeckResponse(deckId, ownerId, deckName2, false, List.of(deckCardResponse)));
        Page<DeckResponse> response = new PageImpl<>(deckResponseList, pageable, deckResponseList.size());

        when(deckService.getByOwnerId(eq(ownerId), any(Pageable.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/decks/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(2));

    }

    @Test
    @WithMockCustomUser
    void getMyDecks_shouldReturnOkAndEmptyPage_whenUserHasNoDecks() throws Exception {

        // Given
        UUID ownerId = getAuthenticatedUserId();

        when(deckService.getByOwnerId(eq(ownerId), any(Pageable.class))).thenReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/decks/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockCustomUser
    void getMyDecks_shouldReturnSpecificDeck_whenDeckNameHasValue() throws Exception{

        // Given
        UUID deckId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        String deckName = "Default Deck";
        DeckCardResponse deckCardResponse = new DeckCardResponse(getDefaultCardResponse(getDefaultCard()), 4);

        Pageable pageable = PageRequest.of(0, 10);
        List<DeckResponse> deckResponseList = new ArrayList<>();
        deckResponseList.add(new DeckResponse(deckId, ownerId, deckName, false, List.of(deckCardResponse)));
        Page<DeckResponse> response = new PageImpl<>(deckResponseList, pageable, deckResponseList.size());

        when(deckService.getByOwnerIdAndDeckName(eq(ownerId), eq(deckName), any(Pageable.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/decks/me")
                        .param("name", deckName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1));

    }

    @Test
    @WithMockCustomUser
    void updateDeck_shouldReturnOkAndDeckResponse_whenRequestIsValid() throws Exception{

        // Given
        UUID cardId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        String deckName = "Updated Deck";
        DeckCardResponse deckCardResponse = new DeckCardResponse(getDefaultCardResponse(getDefaultCard()), 4);

        DeckRequest request = new DeckRequest(deckName, List.of(new DeckCardRequest(cardId, 4)));
        DeckResponse response = new DeckResponse(deckId, ownerId, deckName, false, List.of(deckCardResponse));

        when(deckService.updateDeck(eq(deckId), any(DeckRequest.class), eq(ownerId))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/decks/{deckId}", deckId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(deckId.toString()))
                .andExpect(jsonPath("$.name").value(deckName))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()));
    }

    @Test
    @WithMockCustomUser
    void updateDeck_shouldThrowResourceNotFound_whenDeckDoesNotExists() throws Exception{

        // Given
        UUID cardId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        String deckName = "Updated Deck";

        DeckRequest request = new DeckRequest(deckName, List.of(new DeckCardRequest(cardId, 4)));

        when(deckService.updateDeck(eq(deckId), any(DeckRequest.class), eq(ownerId))).thenThrow(new ResourceNotFoundException("Deck with ID: " + deckId + " not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/decks/{deckId}", deckId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void updateDeck_shouldThrowDuplicateResource_whenDeckNameAlreadyExists() throws Exception{

        // Given
        UUID cardId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        String deckName = "Updated Deck";

        DeckRequest request = new DeckRequest(deckName, List.of(new DeckCardRequest(cardId, 4)));

        when(deckService.updateDeck(eq(deckId), any(DeckRequest.class), eq(ownerId))).thenThrow(new DuplicateResourceException("Deck already exists with the name: " + deckName));

        // When & Then
        mockMvc.perform(put("/api/v1/decks/{deckId}", deckId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockCustomUser
    void updateDeck_shouldThrowResourceNotFound_whenCardDoesNotExists() throws Exception{

        // Given
        UUID cardId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();
        String deckName = "Updated Deck";

        DeckRequest request = new DeckRequest(deckName, List.of(new DeckCardRequest(cardId, 4)));

        when(deckService.updateDeck(eq(deckId), any(DeckRequest.class), eq(ownerId))).thenThrow(new ResourceNotFoundException("Card with ID: " + cardId + " not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/decks/{deckId}", deckId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void deleteDeck_shouldReturnNoContent_whenRequestIsValid() throws Exception{

        // Given
        UUID deckId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();

        doNothing().when(deckService).deleteDeck(deckId, ownerId);

        // When & Then
        mockMvc.perform(delete("/api/v1/decks/{deckId}", deckId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser
    void deleteDeck_shouldThrowResourceNotFound_whenDeckDoesNotExists() throws Exception{

        // Given
        UUID deckId = UUID.randomUUID();
        UUID ownerId = getAuthenticatedUserId();

        doThrow(new ResourceNotFoundException("Deck with ID: " + deckId + " not found")).when(deckService).deleteDeck(deckId, ownerId);

        // When & Then
        mockMvc.perform(delete("/api/v1/decks/{deckId}", deckId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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

    private DeckCard getDefaultDeckCard(Card card, Deck deck, int quantity) {

        return DeckCard.builder()
                .card(card)
                .deck(deck)
                .quantity(quantity)
                .build();
    }

    private UUID getAuthenticatedUserId() {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Objects.requireNonNull(user);
        return user.getId();
    }
}
