package com.lemicare.delivery.service.strategy.impl;

import com.cosmicdoc.common.model.DeliveryOrder;
import com.cosmicdoc.common.model.DeliveryStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lemicare.delivery.service.client.OrderServiceClient;
import com.lemicare.delivery.service.client.OrganizationServiceClient;
import com.lemicare.delivery.service.client.ShiprocketApiClient;
import com.lemicare.delivery.service.client.dto.BranchConfig;
import com.lemicare.delivery.service.client.dto.OrderDetailsDto;
import com.lemicare.delivery.service.dto.request.ShiprocketAssignAwbRequest;
import com.lemicare.delivery.service.dto.request.ShiprocketCreateOrderRequest;
import com.lemicare.delivery.service.dto.response.ShiprocketAssignAwbResponse;
import com.lemicare.delivery.service.dto.response.ShiprocketCreateOrderResponse;
import com.lemicare.delivery.service.exception.PartnerApiException;
import com.lemicare.delivery.service.strategy.DeliveryPartnerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShiprocketStrategyImpl implements DeliveryPartnerStrategy {

    private static final String PARTNER_NAME = "SHIPROCKET";
    private final ShiprocketApiClient shiprocketApiClient;
    private final OrganizationServiceClient organizationServiceClient;
    private final OrderServiceClient orderServiceClient;

    @Override
    public String getPartnerName() {
        return PARTNER_NAME;
    }

    @Override
    public int createShipment(String orgId,DeliveryOrder deliveryOrder) throws PartnerApiException {
        log.info("Using ShiprocketStrategy to create shipment for internal orderId: {}", deliveryOrder.getOrderId());

        // 1. Map our internal model to the partner-specific request DTO.
        ShiprocketCreateOrderRequest shiprocketRequest = mapToShiprocketRequest(orgId,deliveryOrder);

        // 2. Call the Shiprocket API client.
        ShiprocketCreateOrderResponse response = shiprocketApiClient.createOrder(shiprocketRequest).block();

        // 3. Validate the response and extract the tracking identifier.
        if (response == null || response.getShipmentId() == null) {
            log.error("Shiprocket API call succeeded but returned a null or empty shipment id  for orderId: {}", deliveryOrder.getOrderId());
            throw new PartnerApiException("Shiprocket returned an invalid tracking code.", PARTNER_NAME, null);
        }

        int shipmentId = response.getShipmentId();
        log.info("Successfully created Shiprocket shipment for orderId: {}. shipment Code: {}", deliveryOrder.getOrderId(), shipmentId);

        // 4. The AWB code is the partner's unique tracking ID.
        return shipmentId;
    }

   public  String assignAwb (String orgId, ShiprocketAssignAwbRequest shiprocketAssignAwbRequest) {
        log.info("Using ShiprocketStrategy to request to update AWB Code: {}", shiprocketAssignAwbRequest.getShipment_id());

        // Delegate the actual API call to the client.
       ShiprocketAssignAwbResponse shiprocketAssignAwbResponse = shiprocketApiClient.assignAwb(shiprocketAssignAwbRequest).block();

        String awbCode = null;
       // log.info("Successfully  request to Shiprocket for AWB Code: {}", shiprocketAssignAwbRequest.getPartnerTrackingId());
     return awbCode;
    }

    /**
     * Cancels a shipment on the Shiprocket platform.
     *
     * @param deliveryOrder The order containing the partnerTrackingId to be cancelled.
     * @param reason The reason for cancellation.
     */
    @Override
    public void cancelShipment(DeliveryOrder deliveryOrder, String reason) throws PartnerApiException {
        if (deliveryOrder.getPartnerTrackingId() == null || deliveryOrder.getPartnerTrackingId().isBlank()) {
            throw new IllegalStateException("Cannot cancel order with no partnerTrackingId (AWB Code). OrderId: " + deliveryOrder.getOrderId());
        }
        log.info("Using ShiprocketStrategy to request cancellation for AWB Code: {}", deliveryOrder.getPartnerTrackingId());

        // Delegate the actual API call to the client.
        shiprocketApiClient.cancelOrder(deliveryOrder.getPartnerTrackingId());
        log.info("Successfully submitted cancellation request to Shiprocket for AWB Code: {}", deliveryOrder.getPartnerTrackingId());
    }


    /**
     * Fetches the shipment status from Shiprocket and maps it to our internal enum.
     *
     * @param deliveryOrder The order to track.
     * @return The mapped internal DeliveryStatus.
     */
    @Override
    public DeliveryStatus getShipmentStatus(DeliveryOrder deliveryOrder) throws PartnerApiException {
        if (deliveryOrder.getPartnerTrackingId() == null || deliveryOrder.getPartnerTrackingId().isBlank()) {
            return deliveryOrder.getStatus(); // Cannot track without an AWB code.
        }
        log.info("Using ShiprocketStrategy to get status for AWB Code: {}", deliveryOrder.getPartnerTrackingId());

        // 1. Get the status string from the client.
        String shiprocketStatus = String.valueOf(shiprocketApiClient.trackOrder(deliveryOrder.getPartnerTrackingId()));

        // 2. Map the partner's status string to our internal, standardized enum.
        DeliveryStatus internalStatus = mapShiprocketStatusToInternalStatus(shiprocketStatus);
        log.info("Mapped Shiprocket status '{}' to internal status '{}' for AWB Code: {}",
                shiprocketStatus, internalStatus, deliveryOrder.getPartnerTrackingId());

        return internalStatus;
    }

    /**
     * Maps an internal DeliveryOrder to the Shiprocket-specific request format.
     * This method acts as an orchestrator, fetching necessary details from other
     * microservices (Organization and Order services) before building the final
     * request for the delivery partner.
     *
     * @param deliveryOrder Our internal data model containing the core delivery info.
     * @return The DTO ready to be sent to the Shiprocket API.
     */
    private ShiprocketCreateOrderRequest mapToShiprocketRequest(String orgId,DeliveryOrder deliveryOrder) {
        log.info("Mapping internal orderId {} to Shiprocket request by calling dependent services.", deliveryOrder.getOrderId());

        // ===================================================================
        // 1. FETCH BRANCH-SPECIFIC CONFIGURATION
        // ===================================================================
        log.debug("Fetching branch config for org: {}, branch: {}", deliveryOrder.getOrganizationId(), deliveryOrder.getBranchId());

        // This is a clean, synchronous call using the Feign client interface.
        // Feign handles the HTTP request, authentication, and error decoding behind the scenes.
       /* BranchConfig branchConfig = organizationServiceClient.getBranchConfig(
                deliveryOrder.getOrganizationId(),
                deliveryOrder.getBranchId()
        );
*/
        // Robustness check: Ensure we got a valid configuration.
       /* if (branchConfig == null || branchConfig.getShiprocketPickupLocation() == null || branchConfig.getShiprocketPickupLocation().isBlank()) {
            throw new IllegalStateException("Missing or invalid Shiprocket pickup location configuration for branchId: " + deliveryOrder.getBranchId());
        }
        String pickupLocation = branchConfig.getShiprocketPickupLocation();
        log.debug("Successfully fetched pickup location: {}", pickupLocation);
*/
        String pickupLocation = "15-2 Dr. Hanan Dermatology ,Gem Bhoomi & Buildings";
        // ===================================================================
        // 2. FETCH ENRICHED ORDER AND PRODUCT DETAILS
        // ===================================================================
        log.debug("Fetching full order details for orderId: {}", deliveryOrder.getOrderId());

        // Another clean call using the Order Service Feign client.
        OrderDetailsDto orderDetails = orderServiceClient.getOrderDetails(orgId,deliveryOrder.getOrderId());

        if (orderDetails == null) {
            throw new IllegalStateException("Could not retrieve details for orderId from order-service: " + deliveryOrder.getOrderId());
        }
        log.debug("Successfully fetched details for {} items.", orderDetails.getItems().size());

        // ===================================================================
        // 3. MAP THE DATA TO THE PARTNER'S FORMAT
        // ===================================================================

        // Map the line items from our internal format to Shiprocket's format.
        List<ShiprocketCreateOrderRequest.OrderItem> shiprocketItems = orderDetails.getItems().stream()
                .map(item -> ShiprocketCreateOrderRequest.OrderItem.builder()
                        .name(item.getName())
                        .sku(item.getSku())
                        .units(item.getQuantity())
                        .sellingPrice(String.valueOf(item.getUnitPrice())) // Shiprocket expects a string
                        .hsn(item.getHsnCode())
                        .build())
                .collect(Collectors.toList());

        // ===================================================================
        // 4. BUILD THE FINAL REQUEST OBJECT
        // ===================================================================
        ShiprocketCreateOrderRequest request = ShiprocketCreateOrderRequest.builder()

                // Core Order Info
                .orderId(deliveryOrder.getOrderId()) // Use our internal ID for their reference
                .orderDate(LocalDate.now().toString())
                .pickupLocationName(pickupLocation) // Use the dynamically fetched config

                // Billing / Recipient Details
                .billingCustomerName(deliveryOrder.getRecipientName())
                .billingLastName("O")
                .billingAddress(orderDetails.getBillingAddressLine1())
                .billingAddress2(orderDetails.getBillingAddressLine2())
                .billingCity(orderDetails.getBillingCity())
                .billingPincode(orderDetails.getBillingPincode())
                .billingState(orderDetails.getBillingState())
                .billingCountry("India")
                .billingPhone(orderDetails.getCustomerPhone())
                .billingEmail(orderDetails.getCustomerEmail())
               // .billingPhone(deliveryOrder.getRecipientPhone())


                .shippingCustomerName(deliveryOrder.getRecipientName())

                .shippingAddress(orderDetails.getBillingAddressLine1())
                .shippingAddress2(orderDetails.getBillingAddressLine2())
                . shippingCity(orderDetails.getBillingCity())
                .shippingPincode(orderDetails.getBillingPincode())
                .shippingState(orderDetails.getBillingState())
                .shippingPhone(orderDetails.getCustomerPhone())
                .shippingCountry("India")
                // Shipping Details (assuming they are the same as billing)
                .shippingIsBilling(true)

                // Line Items and Financials
                .orderItems(shiprocketItems)
                .paymentMethod(orderDetails.getPaymentMethod()) // e.g., "Prepaid", "COD"
                .subTotal(orderDetails.getTotalOrderValue())

                // Package Dimensions
                .weight(orderDetails.getTotalWeightKg())
                .height(orderDetails.getPackageHeightCm())
                .breadth(orderDetails.getPackageBreadthCm())
                .length(orderDetails.getPackageLengthCm())
                .build();

       /* log.info("--- Shiprocket Create Order Request Payload DEBUG ---");
        log.info("Order ID: {}", request.getOrderId());
        log.info("Pickup Location: {}", request.getPickupLocationName());
        log.info("Billing Customer Name: {}", request.getBillingCustomerName());
        log.info("Billing Address 1: {}", request.getBillingAddress());
        log.info("Billing Address 2: {}", request.getBillingAddress2());
        log.info("Billing City: {}", request.getBillingCity());
        log.info("Billing Pincode: {}", request.getBillingPincode());
        log.info("Billing State: {}", request.getBillingState());
        log.info("Billing Country: {}", request.getBillingCountry());
        log.info("Billing Email: {}", request.getBillingEmail());
        log.info("Billing Phone: {}", request.getBillingPhone());
        log.info("Shipping is Billing: {}", request.getShippingIsBilling());
        log.info("Payment Method: {}", request.getPaymentMethod());
        log.info("Sub Total: {}", request.getSubTotal());
        log.info("Weight (Kg): {}", request.getWeight());
        log.info("Length (Cm): {}", request.getLength());
        log.info("Breadth (Cm): {}", request.getBreadth());
        log.info("Height (Cm): {}", request.getHeight());
        log.info("Number of Items: {}", request.getOrderItems() != null ? request.getOrderItems().size() : 0);

// Use Jackson to print the full JSON for ultimate clarity (add 'implementation group: 'com.fasterxml.jackson.core', name: 'jackson-databind', version: '2.x.x'' if not already present)
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // For pretty-printing
        try {
            String jsonPayload = mapper.writeValueAsString(request);
            log.info("Full Shiprocket Request JSON:\n{}", jsonPayload);
        } catch (JsonProcessingException e) {
            log.error("Error converting Shiprocket request to JSON for logging", e);
        }
        log.info("--- END Shiprocket Create Order Request Payload DEBUG ---");*/

        return request; // Return the built request object
    }

    /**
     * A private helper to translate Shiprocket's status strings into our canonical DeliveryStatus enum.
     * This mapping is crucial for standardizing delivery tracking across different partners.
     *
     * @param shiprocketStatus The status string from the Shiprocket tracking API.
     * @return The corresponding internal DeliveryStatus.
     */
    private DeliveryStatus mapShiprocketStatusToInternalStatus(String shiprocketStatus) {
        if (shiprocketStatus == null) {
            return DeliveryStatus.PENDING;
        }
        // This mapping should be expanded based on the full list of statuses from Shiprocket's documentation.
        switch (shiprocketStatus.toUpperCase()) {
            case "NEW":
            case "ACCEPTED":
            case "AWB ASSIGNED":
            case "LABEL GENERATED":
            case "PICKUP SCHEDULED":
            case "PICKUP GENERATED":
                return DeliveryStatus.ACCEPTED;
            case "PICKED UP":
            case "SHIPPED":
                return DeliveryStatus.PICKED_UP;
            case "IN TRANSIT":
            case "OUT FOR DELIVERY":
            case "REACHED AT DESTINATION HUB":
                return DeliveryStatus.IN_TRANSIT;
            case "DELIVERED":
                return DeliveryStatus.DELIVERED;
            case "CANCELLED":
            case "RTO INITIATED":
            case "RTO DELIVERED":
                return DeliveryStatus.CANCELLED;
            default:
                log.warn("Unknown Shiprocket status received: '{}'. Defaulting to IN_TRANSIT.", shiprocketStatus);
                return DeliveryStatus.IN_TRANSIT;
        }
    }


}
