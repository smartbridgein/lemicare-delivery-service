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
public class ServiceabilityData {
    @JsonProperty("available_courier_companies")
    private List<ShiprocketAvailableCourierCompany> availableCourierCompanies;

    // Add any other fields that might be directly inside the 'data' JSON object
    // if you need to capture them. Examples from your raw log:
    // @JsonProperty("is_recommendation_enabled")
    // private Integer isRecommendationEnabled;
    // @JsonProperty("promise_recommended_courier_company_id")
    // private Integer promiseRecommendedCourierCompanyId;
    // @JsonProperty("recommended_courier_company_id")
    // private Integer recommendedCourierCompanyId;
}
