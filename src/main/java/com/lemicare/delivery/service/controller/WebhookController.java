package com.lemicare.delivery.service.controller;

import com.lemicare.delivery.service.dto.request.ShiprocketWebhookPayload;
import com.lemicare.delivery.service.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller dedicated to handling incoming webhook events from external delivery partners.
 * This controller is responsible for receiving, validating, and delegating the processing
 * of real-time status updates.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * Endpoint to receive tracking status updates from Shiprocket.
     * Shiprocket secures webhooks with a custom header containing a shared secret.
     *
     * @param payload The JSON payload sent by Shiprocket, mapped to a DTO.
     * @param authToken The security token from the 'X-Shiprocket-Token' header.
     * @return A 200 OK response to acknowledge receipt. The processing is done asynchronously.
     */
    @PostMapping("/shiprocket")
    public ResponseEntity<Void> handleShiprocketWebhook(
            @RequestBody ShiprocketWebhookPayload payload,
            @RequestHeader("X-Shiprocket-Token") String authToken) {

        // Log the incoming request for traceability, but be careful not to log sensitive data.
        log.info("API: Received Shiprocket webhook for AWB: {}", payload.getAwb());

        // 1. SECURITY: The WebhookService MUST validate the authToken.
        // 2. ASYNC PROCESSING: The service will process the update asynchronously.
        webhookService.processShiprocketUpdate(payload, authToken);

        // 3. IMMEDIATE RESPONSE: Acknowledge receipt immediately with a 200 OK.
        // Do not wait for processing to complete.
        return ResponseEntity.ok().build();
    }

    // You would add another @PostMapping for each new partner, e.g., /dunzo
    // @PostMapping("/dunzo")
    // public ResponseEntity<Void> handleDunzoWebhook(...) { ... }
}
