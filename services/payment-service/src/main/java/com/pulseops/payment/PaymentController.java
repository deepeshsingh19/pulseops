package com.pulseops.payment;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private volatile FailureMode failureMode = FailureMode.NORMAL;

    @PostMapping
    public ResponseEntity<Map<String, Object>> processPayment() {

        if (failureMode == FailureMode.LATENCY) {
            sleep(2500);
        }

        if (failureMode == FailureMode.ERROR) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "FAILED",
                            "message", "Simulated payment provider failure"
                    ));
        }

        return ResponseEntity.ok(
                Map.of(
                        "paymentId", "PAY-" + UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase(),
                        "status", "SUCCESS"
                )
        );
    }

    @PostMapping("/failure-mode")
    public ResponseEntity<Map<String, String>> setFailureMode(
            @RequestBody Map<String, String> request) {

        String mode = request.getOrDefault(
                "mode",
                "NORMAL"
        );

        try {
            failureMode =
                    FailureMode.valueOf(
                            mode.toUpperCase()
                    );
        } catch (IllegalArgumentException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            "Use NORMAL, LATENCY or ERROR"
                    ));
        }

        return ResponseEntity.ok(
                Map.of(
                        "failureMode",
                        failureMode.name()
                )
        );
    }

    @GetMapping("/failure-mode")
    public ResponseEntity<Map<String, String>> getFailureMode() {

        return ResponseEntity.ok(
                Map.of(
                        "failureMode",
                        failureMode.name()
                )
        );
    }

    public FailureMode getCurrentFailureMode() {
        return failureMode;
    }

    private void sleep(long milliseconds) {

        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}