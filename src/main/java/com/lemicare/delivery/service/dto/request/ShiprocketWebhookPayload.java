package com.lemicare.delivery.service.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the webhook payload sent by Shiprocket for tracking updates.
 * This structure is based on Shiprocket's webhook documentation.
 *
 * @see <a href="https://www.shiprocket.in/features/courier-tracking-sms-email-notifications/">Shiprocket Webhooks</a>
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiprocketWebhookPayload {

    /** The Air Waybill (AWB) number, which is our partnerTrackingId. */
    @JsonProperty("awb")
    private String awb;

    /** The unique ID of the order in Shiprocket's system. */
    @JsonProperty("shipment_id")
    private Long shipmentId;

    /** The unique ID of the order in our system. */
    @JsonProperty("order_id")
    private String orderId;

    /** The new, current status of the shipment (e.g., "DELIVERED"). */
    @JsonProperty("current_status")
    private String currentStatus;

    // You can add many more fields from the webhook payload as needed,
    // such as 'current_timestamp', 'location', 'event_time', etc.
}

