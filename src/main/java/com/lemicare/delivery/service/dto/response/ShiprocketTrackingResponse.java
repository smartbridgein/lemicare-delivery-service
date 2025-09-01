package com.lemicare.delivery.service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiprocketTrackingResponse {

    @JsonProperty("tracking_data")
    private TrackingData trackingData;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrackingData {

        // This is the field we are most interested in.
        @JsonProperty("shipment_status")
        private String shipmentStatus;

        // You can add more fields here if you need them, like 'track_url', etc.
    }
}

