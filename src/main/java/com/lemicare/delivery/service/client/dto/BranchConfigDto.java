package com.lemicare.delivery.service.client.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing the configuration details for a specific branch.
 *
 * This class defines the internal API contract for data fetched from the organization-service.
 * It carries essential, branch-specific settings required by other services to perform
 * their functions, such as the correct pickup location name for a delivery partner.
 *
 * The @JsonIgnoreProperties(ignoreUnknown = true) annotation is a critical feature for
 * creating a loosely coupled microservices architecture. It ensures that if the
 * organization-service adds new fields to its response in the future, this client
 * will not break.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchConfigDto {

    /**
     * The unique "Pickup Nickname" for this branch as configured in the Shiprocket dashboard.
     * This field is mandatory for creating Shiprocket shipments and must be an exact match.
     *
     * Example: "LemiCare-Downtown-WH"
     */
    private String shiprocketPickupLocation;

    /**
     * An example of another configuration field you might add in the future.
     * This could define the default courier partner for branches that don't specify one.
     *
     * Example: "SHIPROCKET"
     */
    private String defaultCourierPartner;

    // Note: We do not add validation annotations like @NotBlank here.
    // This is a response DTO from a trusted, internal service. The responsibility for
    // ensuring the data is valid lies with the provider (organization-service).
    // The consumer's responsibility is to handle null or empty values gracefully.

}