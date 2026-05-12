package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.services.CardCatalogSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/sync")
@RequiredArgsConstructor
public class SyncController {

    private final CardCatalogSyncService cardCatalogSyncService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Void> syncAll() {

        cardCatalogSyncService.syncAll();
        return ResponseEntity.noContent().build();
    }
}
