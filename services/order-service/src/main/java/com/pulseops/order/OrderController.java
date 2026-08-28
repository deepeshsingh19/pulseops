package com.pulseops.order;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder() {

        return ResponseEntity.ok(
                Map.of(
                        "orderId", "ORD-" + UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase(),
                        "status", "CREATED"
                )
        );
    }

    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("order-service-ok");
    }
}