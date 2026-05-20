package com.silphengine.infrastructure.web.controllers.api;

import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.dto.responses.ExpansionResponse;
import com.silphengine.domain.exceptions.BadRequestException;
import com.silphengine.domain.services.ExpansionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expansions")
@RequiredArgsConstructor
public class ExpansionController {

    private final ExpansionService expansionService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ExpansionResponse> createExpansion(@Valid @RequestBody ExpansionRequest request) {
        ExpansionResponse response = expansionService.createExpansion(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<ExpansionResponse> getExpansionByExternalId(@PathVariable String externalId) {
        return ResponseEntity.ok(expansionService.getByExternalId(externalId));
    }

    @GetMapping
    public ResponseEntity<List<ExpansionResponse>> getAllExpansions() {
        return ResponseEntity.ok(expansionService.getAllExpansions());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{externalId}")
    public ResponseEntity<ExpansionResponse> updateExpansion(
            @PathVariable String externalId,
            @Valid @RequestBody ExpansionRequest request) {

        if (!externalId.equals(request.externalId())) {
            throw new BadRequestException("The ID in the URL (" + externalId +
                    ") does not match the ID in the body (" + request.externalId() + ")");
        }

        return ResponseEntity.ok(expansionService.updateByExternalId(externalId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{externalId}")
    public ResponseEntity<Void> deleteExpansion(@PathVariable String externalId) {
        expansionService.removeByExternalId(externalId);
        return ResponseEntity.noContent().build();
    }
}
