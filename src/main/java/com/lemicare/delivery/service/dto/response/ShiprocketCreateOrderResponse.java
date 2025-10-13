package com.lemicare.delivery.service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiprocketCreateOrderResponse {
    private boolean status;
    private Integer message; // Sometimes just a success code
    @JsonProperty("order_id")
    private Integer shiprocketOrderId; // Shiprocket's internal order ID
    @JsonProperty("awb_code")
    private String awbCode;
    private String courier; // Courier name
    @JsonProperty("pickup_scheduled_date")
    private String pickupScheduledDate; // YYYY-MM-DD
    @JsonProperty("label_url")
    private String labelUrl;
    @JsonProperty("manifest_url")
    private String manifestUrl;
    @JsonProperty("shipment_id")
    private Integer shipmentId;
    @JsonProperty("charge_details")
    private ChargeDetails chargeDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChargeDetails {
        @JsonProperty("total_charges")
        private BigDecimal totalCharges;
        // Add other charge details as needed
    }
}