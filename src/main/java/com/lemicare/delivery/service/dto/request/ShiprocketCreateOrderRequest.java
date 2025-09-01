package com.lemicare.delivery.service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing the JSON payload for creating an "Adhoc" order
 * via the Shiprocket v1 API. This class is designed to be constructed using the Builder pattern
 * for readability and ease of use.
 *
 * The @JsonInclude(JsonInclude.Include.NON_NULL) annotation ensures that any fields that are
 * null in the Java object will not be included in the serialized JSON, keeping the payload clean.
 *
 * @see <a href="https://apidocs.shiprocket.in/?version=latest#bf2a0d48-c782-449d-83b2-652f14480985">Shiprocket Create Adhoc Order API Docs</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShiprocketCreateOrderRequest {

    // --- Core Order Details ---

    @JsonProperty("order_id")
    private String orderId; // Your internal, unique order ID

    @JsonProperty("order_date")
    private String orderDate; // Format: YYYY-MM-DD

    @JsonProperty("channel_id")
    private String channelId; // Your Shiprocket channel ID, if you have one

    // --- Pickup Location Details ---
    // This must match the name of a pickup location configured in your Shiprocket dashboard.
    @JsonProperty("pickup_location_name")
    private String pickupLocationName;

    // --- Billing Details ---

    @JsonProperty("billing_customer_name")
    private String billingCustomerName;

    @JsonProperty("billing_last_name")
    private String billingLastName; // Often not required if full name is in the first field

    @JsonProperty("billing_address")
    private String billingAddress;

    @JsonProperty("billing_address_2")
    private String billingAddress2;

    @JsonProperty("billing_city")
    private String billingCity;

    @JsonProperty("billing_pincode")
    private String billingPincode;

    @JsonProperty("billing_state")
    private String billingState;

    @JsonProperty("billing_country")
    private String billingCountry;

    @JsonProperty("billing_email")
    private String billingEmail;

    @JsonProperty("billing_phone")
    private String billingPhone;

    // --- Shipping Details ---

    @JsonProperty("shipping_is_billing")
    private Boolean shippingIsBilling; // Set to true if shipping and billing addresses are the same

    // Provide these only if shippingIsBilling is false
    @JsonProperty("shipping_customer_name")
    private String shippingCustomerName;

    @JsonProperty("shipping_last_name")
    private String shippingLastName;

    @JsonProperty("shipping_address")
    private String shippingAddress;

    @JsonProperty("shipping_address_2")
    private String shippingAddress2;

    @JsonProperty("shipping_city")
    private String shippingCity;

    @JsonProperty("shipping_pincode")
    private String shippingPincode;

    @JsonProperty("shipping_state")
    private String shippingState;

    @JsonProperty("shipping_country")
    private String shippingCountry;

    @JsonProperty("shipping_email")
    private String shippingEmail;

    @JsonProperty("shipping_phone")
    private String shippingPhone;


    // --- Order Items ---

    @JsonProperty("order_items")
    private List<OrderItem> orderItems;


    // --- Payment and Financial Details ---

    @JsonProperty("payment_method")
    private String paymentMethod; // "Prepaid" or "COD"

    @JsonProperty("shipping_charges")
    private Double shippingCharges;

    @JsonProperty("giftwrap_charges")
    private Double giftwrapCharges;



    @JsonProperty("transaction_charges")
    private Double transactionCharges;

    @JsonProperty("total_discount")
    private Double totalDiscount;

    @JsonProperty("sub_total")
    private Double subTotal; // The total price of all order_items


    // --- Package Dimensions (in cms and kgs) ---

    @JsonProperty("length")
    private Double length;

    @JsonProperty("breadth")
    private Double breadth;

    @JsonProperty("height")
    private Double height;

    @JsonProperty("weight")
    private Double weight; // Weight in Kgs


    /**
     * Nested static class representing a single line item in the order.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {

        @JsonProperty("name")
        private String name;

        @JsonProperty("sku")
        private String sku;

        @JsonProperty("units")
        private Integer units;

        @JsonProperty("selling_price")
        private String sellingPrice; // Per unit price

        @JsonProperty("discount")
        private String discount;

        @JsonProperty("tax")
        private String tax;

        @JsonProperty("hsn")
        private Integer hsn; // HSN code of the product
    }
}

