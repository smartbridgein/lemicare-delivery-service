package com.lemicare.delivery.service.service;

import com.cosmicdoc.common.model.DeliveryOrder;
import com.cosmicdoc.common.model.DeliveryStatus;
import com.cosmicdoc.common.repository.DeliveryOrderRepository;
import com.lemicare.delivery.service.dto.request.ShiprocketWebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Optional;

/**
 * Service dedicated to processing incoming webhooks from delivery partners.
 * This service is responsible for validating the authenticity of webhooks and
 * processing their payloads asynchronously to update the state of delivery orders.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final DeliveryOrderRepository deliveryOrderRepository;

    //@Value("${shiprocket.api.webhook-token}")
    private String shiprocketWebhookSecret;

    /**
     * Public entry point for processing a Shiprocket webhook.
     * This method performs a synchronous security check and then delegates
     * the actual processing to an asynchronous method.
     *
     * @param payload The webhook payload from Shiprocket.
     * @param authToken The secret token from the 'X-Shiprocket-Token' header.
     */
    public void processShiprocketUpdate(ShiprocketWebhookPayload payload, String authToken) {
        // 1. --- SECURITY CHECK (Synchronous) ---
        // This is the most critical step. If the token doesn't match, reject the request immediately.
        if (!shiprocketWebhookSecret.equals(authToken)) {
            log.warn("SECURITY ALERT: Received Shiprocket webhook with an invalid token. AWB: {}", payload.getAwb());
            throw new AccessDeniedException("Invalid webhook token.");
        }

        // 2. --- DISPATCH FOR ASYNCHRONOUS PROCESSING ---
        // The controller will return 200 OK immediately after this call.
        processUpdateAsync(payload);
    }

    /**
     * The core processing logic, executed in a separate thread.
     * This method handles finding the order, checking for idempotency, updating the status,
     * and saving the changes.
     *
     * @param payload The validated webhook payload.
     */
    @Async
    public void processUpdateAsync(ShiprocketWebhookPayload payload) {
        String awb = payload.getAwb();
        log.info("Async processing started for Shiprocket AWB: {}", awb);

        // 3. --- FIND THE CORRESPONDING ORDER ---
        // Use the AWB (Air Waybill) code, which is our partnerTrackingId.
        Optional<DeliveryOrder> optionalOrder = deliveryOrderRepository.findByPartnerTrackingId(awb);

        if (optionalOrder.isEmpty()) {
            log.warn("Received webhook for an unknown AWB: {}. The order may not exist or has been deleted. Ignoring.", awb);
            return; // Exit gracefully.
        }

        DeliveryOrder order = optionalOrder.get();
        DeliveryStatus newStatus = mapShiprocketStatusToInternalStatus(payload.getCurrentStatus());

        // 4. --- IDEMPOTENCY CHECK ---
        // If the new status is the same as the current one, we have likely already processed this event.
        if (order.getStatus() == newStatus) {
            log.info("Idempotency check: AWB {} is already in status {}. No update needed.", awb, newStatus);
            return;
        }

        // 5. --- UPDATE THE ORDER ---
        log.info("Updating status for orderId: {} (AWB: {}) from {} to {}",
                order.getOrderId(), awb, order.getStatus(), newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(new Date());

        // Set the final delivered timestamp if applicable.
        if (newStatus == DeliveryStatus.DELIVERED) {
            order.setDeliveredAt(new Date());
        }

        // 6. --- SAVE THE CHANGES ---
        deliveryOrderRepository.save(order);
        log.info("Successfully updated status for AWB: {}", awb);
    }

    /**
     * A private helper to translate Shiprocket's status strings into our canonical DeliveryStatus enum.
     * This is a crucial anti-corruption layer.
     *
     * @param shiprocketStatus The status string from the Shiprocket webhook.
     * @return The corresponding internal DeliveryStatus.
     */
    private DeliveryStatus mapShiprocketStatusToInternalStatus(String shiprocketStatus) {
        if (shiprocketStatus == null) {
            return DeliveryStatus.PENDING;
        }
        // This mapping should be expanded based on the full list of statuses from Shiprocket's documentation.
        switch (shiprocketStatus.toUpperCase()) {
            case "NEW":
            case "ACCEPTED":
            case "AWB ASSIGNED":
            case "LABEL GENERATED":
            case "PICKUP SCHEDULED":
            case "PICKUP GENERATED":
                return DeliveryStatus.ACCEPTED;
            case "PICKED UP":
            case "SHIPPED":
                return DeliveryStatus.PICKED_UP;
            case "IN TRANSIT":
            case "OUT FOR DELIVERY":
            case "REACHED AT DESTINATION HUB":
                return DeliveryStatus.IN_TRANSIT;
            case "DELIVERED":
                return DeliveryStatus.DELIVERED;
            case "CANCELLED":
            case "RTO INITIATED":
            case "RTO DELIVERED":
                return DeliveryStatus.CANCELLED;
            default:
                log.warn("Unknown Shiprocket status received in webhook: '{}'. Defaulting to IN_TRANSIT.", shiprocketStatus);
                return DeliveryStatus.IN_TRANSIT;
        }
    }
}
