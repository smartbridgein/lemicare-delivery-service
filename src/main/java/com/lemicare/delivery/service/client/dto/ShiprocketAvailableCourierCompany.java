package com.lemicare.delivery.service.client.dto;

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
public class ShiprocketAvailableCourierCompany {
    // Changed 'id' from String to Integer based on common practice for IDs,
    // but if Shiprocket sends it as a String, keep it as String.
    // From your log, it's 678792066 which is an Integer.
    private Integer id; // Changed type based on example log
    @JsonProperty("courier_name") // Use this if your DTO field is 'courierName'
    private String courierName;
    @JsonProperty("rate")
    private BigDecimal rate; // This is the cost
    @JsonProperty("min_weight")
    private BigDecimal minWeight;
    @JsonProperty("max_weight")
    private BigDecimal maxWeight;
    @JsonProperty("etd") // Estimated Time of Delivery (e.g., "Oct 14, 2025")
    private String etd;
    @JsonProperty("estimated_delivery_days") // This seems to be the one you want for integer days
    private String estimatedDeliveryDays; // Renamed to match the JSON field precisely
    // Changed "etd_days" to "estimated_delivery_days" to match your raw log

    @JsonProperty("rto_tat") // Return to origin TAT
    private String rtoTat; // Not present in provided log, but can be added if it appears

    // Logo was not in your raw log, but common for couriers. Add if needed.
    // private String logo;

    private String description;
    @JsonProperty("base_weight")
    private BigDecimal baseWeight;
    // 'type' from your original DTO might map to a field like 'ship_type' or 'mode' in the actual response.
    // Based on your log, "ship_type":1 and "mode":0.
    @JsonProperty("ship_type") // This seems to be the type from the log
    private Integer shipType;
    @JsonProperty("is_recommendation")
    private Boolean isRecommendation;

    // Add other fields from the raw response you deem useful:
    @JsonProperty("courier_company_id") // Often useful if different from 'id'
    private Integer courierCompanyId;
    @JsonProperty("edd") // Can be different from etd/etd_days
    private String edd;
    @JsonProperty("edd_fallback")
    private Object eddFallback; // Can be a complex object
    @JsonProperty("freight_charge")
    private BigDecimal freightCharge;
    @JsonProperty("zone")
    private String zone;
    @JsonProperty("city")
    private String city;
    @JsonProperty("state")
    private String state;
    @JsonProperty("pickup_performance")
    private BigDecimal pickupPerformance;
    @JsonProperty("delivery_performance")
    private BigDecimal deliveryPerformance;
    @JsonProperty("tracking_performance")
    private BigDecimal trackingPerformance;
    @JsonProperty("rto_performance")
    private BigDecimal rtoPerformance;
    @JsonProperty("rto_charges")
    private BigDecimal rtoCharges;
    @JsonProperty("other_charges")
    private BigDecimal otherCharges;
    @JsonProperty("call_before_delivery")
    private String callBeforeDelivery;
    @JsonProperty("pod_available")
    private String podAvailable;
    @JsonProperty("realtime_tracking")
    private String realtimeTracking;
    @JsonProperty("local_region")
    private Integer localRegion;
    @JsonProperty("metro")
    private Integer metro;
    @JsonProperty("cutoff_time")
    private String cutoffTime;
    @JsonProperty("seconds_left_for_pickup")
    private Integer secondsLeftForPickup;
    @JsonProperty("is_surface")
    private Boolean isSurface;
}