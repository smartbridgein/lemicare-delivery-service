package com.lemicare.delivery.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class CreateDeliveryRequest {
    /**
     * The unique identifier for the order from the calling service (e.g., order-service).
     * This is used to link the delivery back to the original order and ensure idempotency.
     */
    @NotBlank(message = "orderId is required.")
    @Size(min = 1, max = 50, message = "orderId must be between 1 and 50 characters.")
    private String orderId;

    /**
     * The name of the delivery partner to be used for this shipment.
     * This value must match one of the keys recognized by the DeliveryStrategyFactory.
     * Example: "SHIPROCKET"
     */
   // @NotBlank(message = "preferredPartner is required (e.g., SHIPROCKET).")
    private String preferredPartner;

    /**
     * The full pickup address. This should be the address of the branch initiating the request.
     */
   // @NotBlank(message = "pickupAddress is required.")
   // @Size(max = 255, message = "pickupAddress cannot exceed 255 characters.")
    private String pickupAddress;

    /**
     * The full drop-off address for the recipient.
     */
   // @NotBlank(message = "dropoffAddress is required.")
  //  @Size(max = 255, message = "dropoffAddress cannot exceed 255 characters.")
    private String dropoffAddress;

    /**
     * The full name of the person receiving the package.
     */
  //  @NotBlank(message = "recipientName is required.")
    @Size(max = 100, message = "recipientName cannot exceed 100 characters.")
    private String recipientName;

    /**
     * The contact phone number of the recipient.
     * A simple pattern is used for basic validation, but international numbers can vary.
     */
   // @NotBlank(message = "recipientPhone is required.")
    @Pattern(regexp = "^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*$", message = "Invalid phone number format.")
    @Size(min = 7, max = 20, message = "recipientPhone must be between 7 and 20 characters.")
    private String recipientPhone;

}


