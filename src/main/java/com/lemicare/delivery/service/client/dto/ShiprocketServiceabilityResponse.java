package com.lemicare.delivery.service.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiprocketServiceabilityResponse {
    // These are top-level fields (HTTP status will be 200, this is internal status)
    private Integer status;
    private String message;

    // This is the crucial part: 'data' is an object that contains 'available_courier_companies'
    @JsonProperty("data")
    private ServiceabilityData data; // Use a nested DTO for the "data" object

    // Removed: @JsonProperty("available_courier_companies") from here
    // as it's now inside the 'data' object.
    // private List<ShiprocketAvailableCourierCompany> availableCourierCompanies;
}
