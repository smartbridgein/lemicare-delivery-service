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
public class BranchConfig {

    private String branchId; // Include branchId for context
    private String branchName; // Include branchName for context

    /**
     * The unique "Pickup Nickname" for this branch as configured in the Shiprocket dashboard.
     * This field is mandatory for creating Shiprocket shipments and must be an exact match.
     * Example: "LemiCare-Downtown-WH"
     */
    private String shiprocketPickupLocation;

    /**
     * Defines the default courier partner for this branch.
     * Example: "SHIPROCKET", "DELHIVERY", "LOCAL_COURIER"
     */
    private String defaultCourierPartner;

    // Flattened address fields from the embedded primaryPickupAddress
    private String pickupAddressLine1;
    private String pickupAddressLine2;
    private String pickupCity;
    private String pickupState;
    private String pickupPincode;
    private String pickupCountry; // From Address model
    private String pickupContactPerson;
    private String pickupContactPhone;
    private String pickupContactEmail;

    // Note: We do not add validation annotations like @NotBlank here.
    // This is a response DTO from a trusted, internal service. The responsibility for
    // ensuring the data is valid lies with the provider (organization-service).
    // The consumer's responsibility is to handle null or empty values gracefully.
}