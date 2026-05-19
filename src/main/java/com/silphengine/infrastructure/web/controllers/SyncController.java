package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.services.CardCatalogSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/sync")
@RequiredArgsConstructor
public class SyncController {

    private final CardCatalogSyncService cardCatalogSyncService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Map<String, String>> syncAll() {

        cardCatalogSyncService.syncAll();
        return ResponseEntity.accepted().body(Map.of(
                "status", "Accepted",
                "message", "Full catalog synchronization started in the background. Check server logs for progress."
        ));
    }
}
