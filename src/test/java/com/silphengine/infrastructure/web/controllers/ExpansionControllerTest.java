package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.dto.responses.ExpansionResponse;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.ExpansionService;
import com.silphengine.infrastructure.web.config.TestSecurityConfig;
import com.silphengine.infrastructure.web.controllers.api.ExpansionController;
import com.silphengine.security.annotations.WithMockCustomUser;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;
import com.silphengine.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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


@WebMvcTest(ExpansionController.class)
@Import(TestSecurityConfig.class)
public class ExpansionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private ExpansionService expansionService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void createExpansion_shouldReturnCreatedAndExpansionResponse_whenRequestIsValid() throws Exception {

        // Given
        String externalId = "swsh3";
        String name = "Darkness Ablaze";
        String serieName = "Sword & Shield";
        LocalDate releaseDate = LocalDate.of(2020, 8, 14);
        int totalCards = 201;
        String logoUrl = "https://images.pokemontcg.io/swsh3/logo.png";

        ExpansionRequest request = new ExpansionRequest(externalId, name, serieName, releaseDate, totalCards, logoUrl);
        ExpansionResponse response = new ExpansionResponse(externalId, name, serieName, releaseDate, totalCards, logoUrl);

        when(expansionService.createExpansion(any(ExpansionRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/expansions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.externalId").value("swsh3"))
                .andExpect(jsonPath("$.name").value("Darkness Ablaze"))
                .andExpect(jsonPath("$.serieName").value("Sword & Shield"));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void createExpansion_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {

        // Given
        String externalId = "";
        String name = "Darkness Ablaze";
        String serieName = "Sword & Shield";
        LocalDate releaseDate = LocalDate.of(2020, 8, 14);
        int totalCards = 201;
        String logoUrl = "https://images.pokemontcg.io/swsh3/logo.png";

        ExpansionRequest request = new ExpansionRequest(externalId, name, serieName, releaseDate, totalCards, logoUrl);

        // When & Then
        mockMvc.perform(post("/api/v1/expansions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void createExpansion_shouldReturnConflict_whenExpansionAlreadyExists() throws Exception {

        // Given
        String externalId = "existingExternalId";
        String name = "Darkness Ablaze";
        String serieName = "Sword & Shield";
        LocalDate releaseDate = LocalDate.of(2020, 8, 14);
        int totalCards = 201;
        String logoUrl = "https://images.pokemontcg.io/swsh3/logo.png";

        ExpansionRequest request = new ExpansionRequest(externalId, name, serieName, releaseDate, totalCards, logoUrl);
        when(expansionService.createExpansion(any(ExpansionRequest.class)))
                .thenThrow(new DuplicateResourceException("Expansion already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/expansions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockCustomUser
    void createExpansion_shouldReturnForbidden_whenHavingUserRole() throws Exception {

        // Given
        String externalId = "swsh3";
        String name = "Darkness Ablaze";
        String serieName = "Sword & Shield";
        LocalDate releaseDate = LocalDate.of(2020, 8, 14);
        int totalCards = 201;
        String logoUrl = "https://images.pokemontcg.io/swsh3/logo.png";

        ExpansionRequest request = new ExpansionRequest(externalId, name, serieName, releaseDate, totalCards, logoUrl);

        // When & Then
        mockMvc.perform(post("/api/v1/expansions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(expansionService, never()).createExpansion(any());
    }


    @Test
    void getExpansionByExternalId_shouldReturnOkAndExpansionResponse_whenExpansionExists() throws Exception {

        // Given
        String externalId = "swsh3";
        String name = "Darkness Ablaze";
        String serieName = "Sword & Shield";
        LocalDate releaseDate = LocalDate.of(2020, 8, 14);
        int totalCards = 201;
        String logoUrl = "https://images.pokemontcg.io/swsh3/logo.png";

        ExpansionResponse response = new ExpansionResponse(externalId, name, serieName, releaseDate, totalCards, logoUrl);

        when(expansionService.getByExternalId(any(String.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/expansions/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.externalId").value("swsh3"))
                .andExpect(jsonPath("$.name").value("Darkness Ablaze"))
                .andExpect(jsonPath("$.serieName").value("Sword & Shield"));
    }

    @Test
    void getExpansionByExternalId_shouldReturnNotFound_whenExpansionDoesNotExists() throws Exception {

        // Given
        String externalId = "nonExistingId";

        when(expansionService.getByExternalId(externalId)).thenThrow(new ResourceNotFoundException("Expansion not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/expansions/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllExpansions_shouldReturnOkAndListOfExpansionResponse() throws Exception {

        // Given
        String externalId = "swsh3";
        String name = "Darkness Ablaze";
        String serieName = "Sword & Shield";
        LocalDate releaseDate = LocalDate.of(2020, 8, 14);
        int totalCards = 201;
        String logoUrl = "https://images.pokemontcg.io/swsh3/logo.png";

        String externalId2 = "base1";
        String name2 = "Base Set";
        String serieName2 = "Base";
        LocalDate releaseDate2 = LocalDate.of(1999, 1, 9);
        int totalCards2 = 102;
        String logoUrl2 = "https://assets.tcgdex.net/en/base/base1/logo.png";

        List<ExpansionResponse> responseList = new ArrayList<>();
        responseList.add(new ExpansionResponse(externalId, name, serieName, releaseDate, totalCards, logoUrl));
        responseList.add(new ExpansionResponse(externalId2, name2, serieName2, releaseDate2, totalCards2, logoUrl2));

        when(expansionService.getAllExpansions()).thenReturn(responseList);

        // When & Then
        mockMvc.perform(get("/api/v1/expansions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateExpansion_shouldReturnOkAndExpansionResponse_whenUpdatedCorrectly() throws Exception {

        // Given
        String externalId = "swsh3";
        String name = "differentName";
        String serieName = "Sword & Shield";
        LocalDate releaseDate = LocalDate.of(2020, 8, 14);
        int totalCards = 201;
        String logoUrl = "https://images.pokemontcg.io/swsh3/logo.png";

        ExpansionRequest request = new ExpansionRequest(externalId, name, serieName, releaseDate, totalCards, logoUrl);
        ExpansionResponse response = new ExpansionResponse(externalId, name, serieName, releaseDate, totalCards, logoUrl);

        when(expansionService.updateByExternalId(eq(externalId), any(ExpansionRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/expansions/{externalId}", externalId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.externalId").value(externalId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.serieName").value(serieName));

    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateExpansion_shouldReturnBadRequest_whenRequestIsNotValid() throws Exception {

        // Given
        String externalId = "swsh3";
        String name = "";
        String serieName = "Sword & Shield";
        LocalDate releaseDate = LocalDate.of(2020, 8, 14);
        int totalCards = 201;
        String logoUrl = "https://images.pokemontcg.io/swsh3/logo.png";

        ExpansionRequest request = new ExpansionRequest(externalId, name, serieName, releaseDate, totalCards, logoUrl);

        // When & Then
        mockMvc.perform(put("/api/v1/expansions/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateExpansion_shouldReturnBadRequest_whenDifferentExternalIds() throws Exception {

        // Given
        String externalId = "swsh3";
        String name = "Darkness Ablaze";
        String serieName = "Sword & Shield";
        LocalDate releaseDate = LocalDate.of(2020, 8, 14);
        int totalCards = 201;
        String logoUrl = "https://images.pokemontcg.io/swsh3/logo.png";

        ExpansionRequest request = new ExpansionRequest("differentExternalId", name, serieName, releaseDate, totalCards, logoUrl);

        // When & Then
        mockMvc.perform(put("/api/v1/expansions/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateExpansion_shouldReturnNotFound_whenExpansionDoesNotExists() throws Exception {

        // Given
        String externalId = "nonExistingId";
        String name = "Darkness Ablaze";
        String serieName = "Sword & Shield";
        LocalDate releaseDate = LocalDate.of(2020, 8, 14);
        int totalCards = 201;
        String logoUrl = "https://images.pokemontcg.io/swsh3/logo.png";

        ExpansionRequest request = new ExpansionRequest(externalId, name, serieName, releaseDate, totalCards, logoUrl);
        when(expansionService.updateByExternalId(eq(externalId), any(ExpansionRequest.class))).thenThrow(new ResourceNotFoundException("Expansion not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/expansions/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void updateExpansion_shouldReturnForbidden_whenHavingUserRole() throws Exception {

        // Given
        String externalId = "swsh3";
        String name = "differentName";
        String serieName = "Sword & Shield";
        LocalDate releaseDate = LocalDate.of(2020, 8, 14);
        int totalCards = 201;
        String logoUrl = "https://images.pokemontcg.io/swsh3/logo.png";

        ExpansionRequest request = new ExpansionRequest(externalId, name, serieName, releaseDate, totalCards, logoUrl);

        // When & Then
        mockMvc.perform(put("/api/v1/expansions/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(expansionService, never()).updateByExternalId(any(), any());
    }


    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void deleteExpansion_shouldReturnNoContent_whenRequestIsValid() throws Exception {

        // Given
        String externalId = "swsh3";
        doNothing().when(expansionService).removeByExternalId(externalId);

        // When & Then
        mockMvc.perform(delete("/api/v1/expansions/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void deleteExpansion_shouldReturnNotFound_whenExpansionDoesNotExists() throws Exception {

        // Given
        String externalId = "swsh3";
        doThrow(new ResourceNotFoundException("Expansion not found")).when(expansionService).removeByExternalId(externalId);

        // When & Then
        mockMvc.perform(delete("/api/v1/expansions/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    void deleteExpansion_shouldReturnForbidden_whenHavingUserRole() throws Exception {

        // Given
        String externalId = "swsh3";

        // When & Then
        mockMvc.perform(delete("/api/v1/expansions/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(expansionService, never()).removeByExternalId(any());
    }
}
