package com.pulseops.inventory;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getInventory(
            @PathVariable String productId) {

        return ResponseEntity.ok(
                Map.of(
                        "productId", productId,
                        "available", true,
                        "quantity", 42
                )
        );
    }

    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("inventory-service-ok");
    }
}