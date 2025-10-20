package com.lemicare.delivery.service.dto.response;


import com.cosmicdoc.common.model.DeliveryStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * The standard Data Transfer Object (DTO) for all API responses that return
 * information about a DeliveryOrder resource.
 *
 * This class defines the public API contract, exposing a curated set of fields
 * from the internal DeliveryOrder model. It provides a stable and secure representation
 * of a delivery's state to any client (e.g., another microservice or a frontend application).
 *
 * The @JsonInclude(JsonInclude.Include.NON_NULL) annotation ensures that any fields that are
 * null (like deliveryFee or deliveredAt before they are set) are omitted from the JSON response,
 * keeping the payload clean and efficient.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryResponse {

    /** The unique identifier of the delivery resource (e.g., a ULID). */
    private String id;

    /** The unique order identifier from the source system. */
    private String orderId;

    /** The ID of the organization (tenant) this delivery belongs to. */
    private String organizationId;

    /** The ID of the specific branch within the organization that initiated the delivery. */
    private String branchId;

    /** The name of the delivery partner handling the shipment (e.g., "SHIPROCKET"). */
    private String partnerName;

    /** The unique tracking ID provided by the external delivery partner (e.g., AWB code). */
    private String partnerTrackingId;

    /** The current, standardized status of the delivery. */
    private DeliveryStatus status;

    /** The full drop-off address for the recipient. */
    private String dropoffAddress;

    /** The full name of the recipient. */
    private String recipientName;

    /** The fee charged for this delivery. May be null until calculated. */
    private BigDecimal deliveryFee;

    /** Additional notes, such as failure reasons or special instructions. */
    private String notes;

    /** The timestamp when the delivery resource was created. */
    private Date createdAt;

    /** The timestamp of the last update to the delivery resource. */
    private Date updatedAt;

    /** The timestamp when the delivery was successfully completed. Null until delivered. */
    private Date deliveredAt;

    private int courierId;

    private int shipmentId;
}
