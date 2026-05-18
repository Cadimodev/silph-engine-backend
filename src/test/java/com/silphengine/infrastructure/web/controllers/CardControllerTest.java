package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.dto.requests.CardRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.BadRequestException;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.CardService;
import com.silphengine.infrastructure.web.config.TestSecurityConfig;
import com.silphengine.security.JwtService;
import com.silphengine.security.annotations.WithMockCustomUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(CardController.class)
@Import(TestSecurityConfig.class)
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void createCard_shouldReturnCreatedAndCardResponse_whenRequestIsValid() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "Magikarp";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        String cardCategory = "Pokemon";
        CardCategory cardCategoryEnum = CardCategory.POKEMON;
        List<String> types = List.of("Water");
        List<CardType> typesEnum = List.of(CardType.WATER);
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";


        CardRequest request = new CardRequest(externalId, name, expansionExternalId, rarity, cardCategory, types, imageUrl, regulationMark);
        CardResponse response = new CardResponse(externalId, name, rarity, cardCategoryEnum, typesEnum, imageUrl, expansionExternalId, regulationMark);

        when(cardService.createCard(any(CardRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.externalId").value(externalId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.expansionExternalId").value(expansionExternalId));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void createCard_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {

        //Given
        String externalId = "";
        String name = "Magikarp";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        String cardCategory = "Pokemon";
        List<String> types = List.of("Water");
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        CardRequest request = new CardRequest(externalId, name, expansionExternalId, rarity, cardCategory, types, imageUrl, regulationMark);

        // When & Then
        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void createCard_shouldReturnNotFound_whenExpansionDoesNotExists() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "Magikarp";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        String cardCategory = "Pokemon";
        List<String> types = List.of("Water");
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        CardRequest request = new CardRequest(externalId, name, expansionExternalId, rarity, cardCategory, types, imageUrl, regulationMark);
        when(cardService.createCard(any(CardRequest.class))).thenThrow(new ResourceNotFoundException("Expansion not found"));

        // When & Then
        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void createCard_shouldReturnConflict_whenCardAlreadyExists() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "Magikarp";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        String cardCategory = "Pokemon";
        List<String> types = List.of("Water");
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        CardRequest request = new CardRequest(externalId, name, expansionExternalId, rarity, cardCategory, types, imageUrl, regulationMark);
        when(cardService.createCard(any(CardRequest.class))).thenThrow(new DuplicateResourceException("Card already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockCustomUser
    void createCard_shouldReturnForbidden_whenHavingUserRole() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "Magikarp";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        String cardCategory = "Pokemon";
        CardCategory cardCategoryEnum = CardCategory.POKEMON;
        List<String> types = List.of("Water");
        List<CardType> typesEnum = List.of(CardType.WATER);
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        CardRequest request = new CardRequest(externalId, name, expansionExternalId, rarity, cardCategory, types, imageUrl, regulationMark);

        // When & Then
        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCardByExternalId_shouldReturnOkAndCardResponse_whenCardExists() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "Magikarp";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        CardCategory cardCategoryEnum = CardCategory.POKEMON;
        List<CardType> typesEnum = List.of(CardType.WATER);
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        CardResponse response = new CardResponse(externalId, name, rarity, cardCategoryEnum, typesEnum, imageUrl, expansionExternalId, regulationMark);
        when(cardService.getByExternalId(eq(externalId))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/cards/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.externalId").value(externalId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.expansionExternalId").value(expansionExternalId));
    }

    @Test
    void getCardByExternalId_shouldReturnNotFound_whenCardDoesNotExists() throws Exception {

        //Given
        String externalId = "sv02-203";

        when(cardService.getByExternalId(eq(externalId))).thenThrow(new ResourceNotFoundException("Card not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/cards/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCards_shouldReturnOkAndPageOfCardResponse_whenExpansionParamIsEmpty() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "Magikarp";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        CardCategory cardCategoryEnum = CardCategory.POKEMON;
        List<CardType> typesEnum = List.of(CardType.WATER);
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        String externalId2 = "sv02-269";
        String name2 = "Iono";
        String rarity2 = "Special Illustration rare";
        CardCategory cardCategoryEnum2 = CardCategory.TRAINER;
        List<CardType> typesEnum2 = List.of();
        String imageUrl2 = "https://assets.tcgdex.net/en/sv/sv02/269/high.png";

        Pageable pageable = PageRequest.of(0, 10);
        List<CardResponse> cardResponseList = new ArrayList<>();
        cardResponseList.add(new CardResponse(externalId, name, rarity, cardCategoryEnum, typesEnum, imageUrl, expansionExternalId, regulationMark));
        cardResponseList.add(new CardResponse(externalId2, name2, rarity2, cardCategoryEnum2, typesEnum2, imageUrl2, expansionExternalId, regulationMark));
        Page<CardResponse> response = new PageImpl<>(cardResponseList, pageable, cardResponseList.size());

        when(cardService.getAllCards(any(Pageable.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getCards_shouldReturnOkAndPageOfCardResponse_whenExpansionParamHasValue() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "Magikarp";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        CardCategory cardCategoryEnum = CardCategory.POKEMON;
        List<CardType> typesEnum = List.of(CardType.WATER);
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        String externalId2 = "sv02-269";
        String name2 = "Iono";
        String rarity2 = "Special Illustration rare";
        CardCategory cardCategoryEnum2 = CardCategory.TRAINER;
        List<CardType> typesEnum2 = List.of();
        String imageUrl2 = "https://assets.tcgdex.net/en/sv/sv02/269/high.png";

        Pageable pageable = PageRequest.of(0, 10);
        List<CardResponse> cardResponseList = new ArrayList<>();
        cardResponseList.add(new CardResponse(externalId, name, rarity, cardCategoryEnum, typesEnum, imageUrl, expansionExternalId, regulationMark));
        cardResponseList.add(new CardResponse(externalId2, name2, rarity2, cardCategoryEnum2, typesEnum2, imageUrl2, expansionExternalId, regulationMark));
        Page<CardResponse> response = new PageImpl<>(cardResponseList, pageable, cardResponseList.size());

        when(cardService.getByExternalExpansionId(eq(expansionExternalId), any(Pageable.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/cards")
                        .param("expansion", expansionExternalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateCardByExternalId_shouldReturnOkCardResponse_whenUpdatedCorrectly() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "NewName";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        String cardCategory = "Pokemon";
        CardCategory cardCategoryEnum = CardCategory.POKEMON;
        List<String> types = List.of("Water");
        List<CardType> typesEnum = List.of(CardType.WATER);
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        CardRequest request = new CardRequest(externalId, name, expansionExternalId, rarity, cardCategory, types, imageUrl, regulationMark);
        CardResponse response = new CardResponse(externalId, name, rarity, cardCategoryEnum, typesEnum, imageUrl, expansionExternalId, regulationMark);

        when(cardService.updateByExternalId(eq(externalId), any(CardRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/cards/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.externalId").value(externalId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.expansionExternalId").value(expansionExternalId));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateCardByExternalId_shouldReturnBadRequest_whenRequestIsNotValid() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        String cardCategory = "Pokemon";
        List<String> types = List.of("Water");
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        CardRequest request = new CardRequest(externalId, name, expansionExternalId, rarity, cardCategory, types, imageUrl, regulationMark);

        // When & Then
        mockMvc.perform(put("/api/v1/cards/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateCardByExternalId_shouldReturnBadRequest_whenDifferentExternalIds() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "NewName";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        String cardCategory = "Pokemon";
        List<String> types = List.of("Water");
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        CardRequest request = new CardRequest("differentExternalId", name, expansionExternalId, rarity, cardCategory, types, imageUrl, regulationMark);

        // When & Then
        mockMvc.perform(put("/api/v1/cards/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateCardByExternalId_shouldReturnBadRequest_whenUpdatingExpansionId() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "Magikarp";
        String expansionExternalId = "newExpansionId";
        String rarity = "Illustration rare";
        String cardCategory = "Pokemon";
        List<String> types = List.of("Water");
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        CardRequest request = new CardRequest(externalId, name, expansionExternalId, rarity, cardCategory, types, imageUrl, regulationMark);

        when(cardService.updateByExternalId(eq(externalId), any(CardRequest.class))).thenThrow(new BadRequestException("A card's expansion cannot be changed"));

        // When & Then
        mockMvc.perform(put("/api/v1/cards/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateCardByExternalId_shouldReturnNotFound_whenCardDoesNotExists() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "Magikarp";
        String expansionExternalId = "newExpansionId";
        String rarity = "Illustration rare";
        String cardCategory = "Pokemon";
        List<String> types = List.of("Water");
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        CardRequest request = new CardRequest(externalId, name, expansionExternalId, rarity, cardCategory, types, imageUrl, regulationMark);

        when(cardService.updateByExternalId(eq(externalId), any(CardRequest.class))).thenThrow(new ResourceNotFoundException("Card not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/cards/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void updateCardByExternalId_shouldReturnForbidden_whenHavingUserRole() throws Exception {

        //Given
        String externalId = "sv02-203";
        String name = "NewName";
        String expansionExternalId = "sv02";
        String rarity = "Illustration rare";
        String cardCategory = "Pokemon";
        List<String> types = List.of("Water");
        String imageUrl = "https://assets.tcgdex.net/en/sv/sv02/203/high.png";
        String regulationMark = "G";

        CardRequest request = new CardRequest(externalId, name, expansionExternalId, rarity, cardCategory, types, imageUrl, regulationMark);

        // When & Then
        mockMvc.perform(put("/api/v1/cards/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(cardService, never()).updateByExternalId(any(), any());

    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void deleteCard_shouldReturnNoContent_whenRequestIsValid() throws Exception {

        // Given
        String externalId = "sv02-203";
        doNothing().when(cardService).deleteByExternalId(externalId);

        // When & Then
        mockMvc.perform(delete("/api/v1/cards/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void deleteCard_shouldReturnNotFound_whenExpansionDoesNotExists() throws Exception {

        // Given
        String externalId = "sv02-203";
        doThrow(new ResourceNotFoundException("Card not found")).when(cardService).deleteByExternalId(externalId);

        // When & Then
        mockMvc.perform(delete("/api/v1/cards/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void deleteCard_shouldReturnForbidden_whenHavingUserRole() throws Exception {

        // Given
        String externalId = "sv02-203";
        doNothing().when(cardService).deleteByExternalId(externalId);

        // When & Then
        mockMvc.perform(delete("/api/v1/cards/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

}
