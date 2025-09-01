package com.lemicare.delivery.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for submitting a delivery cancellation request.
 *
 * This class defines the public API contract for the request body of the
 * POST /api/v1/deliveries/{deliveryId}/cancel endpoint.
 *
 * Its primary purpose is to enforce that a reason is always provided when a
 * delivery is cancelled, which is crucial for auditing and operational tracking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequest {

    /**
     * A mandatory, human-readable reason explaining why the delivery is being cancelled.
     * This information will be stored in the 'notes' field of the DeliveryOrder for
     * auditing and customer service purposes.
     *
     * Example: "Customer requested cancellation via phone call."
     */
    @NotBlank(message = "Cancellation reason is required.")
    @Size(min = 5, max = 255, message = "Reason must be between 5 and 255 characters.")
    private String reason;

}

