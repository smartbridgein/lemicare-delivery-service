package com.lemicare.delivery.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemicare.delivery.service.client.dto.DeliveryOption;
import com.lemicare.delivery.service.client.dto.ShiprocketAvailableCourierCompany;
import com.lemicare.delivery.service.client.dto.ShiprocketServiceabilityResponse;
import com.lemicare.delivery.service.config.ShiprocketConfig;
import com.lemicare.delivery.service.client.dto.CourierServiceabilityRequest;
import com.lemicare.delivery.service.dto.request.ShiprocketAssignAwbRequest;
import com.lemicare.delivery.service.dto.request.ShiprocketAuthRequest;
import com.lemicare.delivery.service.dto.request.ShiprocketCancelRequest;
import com.lemicare.delivery.service.dto.request.ShiprocketCreateOrderRequest;
import com.lemicare.delivery.service.dto.response.ShiprocketAssignAwbResponse;
import com.lemicare.delivery.service.dto.response.ShiprocketAuthResponse;
import com.lemicare.delivery.service.dto.response.ShiprocketCreateOrderResponse;
import com.lemicare.delivery.service.dto.response.ShiprocketTrackingResponse;
import com.lemicare.delivery.service.exception.PartnerApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ShiprocketApiClient {

    private final WebClient webClient;
    private final ShiprocketConfig shiprocketConfig;
    private final ObjectMapper objectMapper;

    // Thread-safe token cache
    private volatile String authToken;
    private volatile LocalDateTime tokenExpiryTime;

    // Define the error handler as a Function field
    private final Function<ClientResponse, Mono<? extends Throwable>> handleAuthError;
    private final Function<ClientResponse, Mono<? extends Throwable>> handleCreateOrderError;
    private final Function<ClientResponse, Mono<? extends Throwable>> handleCancelOrderError;
    private final Function<ClientResponse, Mono<? extends Throwable>> handleTrackOrderError;
    private final Function<ClientResponse, Mono<? extends Throwable>> handleServiceabilityError;


    public ShiprocketApiClient(WebClient.Builder shiprocketWebClientBuilder,
                               ShiprocketConfig shiprocketConfig,
                               ObjectMapper objectMapper) {
        // The base URL should now only be "https://apiv2.shiprocket.in"
        // as "/v1/external" is added in each .uri() call.
        this.webClient = shiprocketWebClientBuilder
                .baseUrl(shiprocketConfig.getBaseUrl()) // Access directly
                .build();
        this.shiprocketConfig = shiprocketConfig;
        this.objectMapper = objectMapper;

        // Initialize the Function fields here, capturing 'this'
        this.handleAuthError = clientResponse -> this.processApiError(clientResponse, "Shiprocket authentication failed");
        this.handleCreateOrderError = clientResponse -> this.processApiError(clientResponse, "Failed to create Shiprocket order");
        this.handleCancelOrderError = clientResponse -> this.processApiError(clientResponse, "Failed to cancel Shiprocket order");
        this.handleTrackOrderError = clientResponse -> this.processApiError(clientResponse, "Failed to track Shiprocket order");
        this.handleServiceabilityError = clientResponse -> this.processApiError(clientResponse, "Failed to get Shiprocket serviceability");
    }

    /**
     * Authenticates with Shiprocket and retrieves an auth token.
     * Implements double-checked locking for thread-safe token management.
     */
    private Mono<String> authenticate() {
        // First check (without lock)
        if (authToken != null && tokenExpiryTime != null && LocalDateTime.now().isBefore(tokenExpiryTime)) {
            log.debug("Shiprocket: Reusing existing valid token.");
            return Mono.just(authToken);
        }

        // Synchronized block for actual token refresh
        return Mono.defer(() -> {
            synchronized (this) {
                // Second check (with lock)
                if (authToken != null && tokenExpiryTime != null && LocalDateTime.now().isBefore(tokenExpiryTime)) {
                    log.debug("Shiprocket: Reusing existing valid token after synchronized check.");
                    return Mono.just(authToken);
                }

                log.info("Shiprocket: Authenticating to get a new token...");
                ShiprocketAuthRequest authRequest = ShiprocketAuthRequest.builder()
                        .email(shiprocketConfig.getEmail()) // Access directly
                        .password(shiprocketConfig.getPassword()) // Access directly
                        .build();

                try {
                    log.debug("Shiprocket: Auth Request Body: {}", objectMapper.writeValueAsString(authRequest));
                } catch (JsonProcessingException e) {
                    log.warn("Shiprocket: Failed to log auth request body: {}", e.getMessage());
                }

                return webClient.post()
                        .uri("/v1/external/auth/login") // Correct full path including /v1/external
                        .bodyValue(authRequest)
                        .retrieve()
                        .onStatus(status -> status.isError() , this.handleAuthError)
                        .bodyToMono(ShiprocketAuthResponse.class)
                        .map(response -> {
                            if (response == null || response.getToken() == null || response.getToken().isBlank()) {
                                throw new PartnerApiException("Received null or empty token from Shiprocket", "SHIPROCKET", HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                            this.authToken = response.getToken();
                            // Set expiry time based on config, typically a few minutes before actual expiry for proactive refresh
                            this.tokenExpiryTime = LocalDateTime.now().plusMinutes(shiprocketConfig.getAuthTokenCacheExpiryMinutes()); // Access directly
                            log.info("Shiprocket: Successfully authenticated. Token valid until: {}", this.tokenExpiryTime);
                            return this.authToken;
                        })
                        .doOnError(e -> {
                            log.error("Shiprocket: Exception during authentication: {}", e.getMessage());
                            if (e instanceof PartnerApiException) throw (PartnerApiException) e;
                            throw new PartnerApiException("Unexpected error during Shiprocket authentication", "SHIPROCKET", HttpStatus.INTERNAL_SERVER_ERROR, e);
                        });
            }
        });
    }

    /**
     * Helper to get a valid authentication token, triggering refresh if needed.
     * This method is the entry point for other methods requiring a token.
     */
    private Mono<String> getValidAuthToken() {
        return authenticate(); // `authenticate()` already handles caching and refresh logic
    }

    /**
     * Centralized error processing method for Shiprocket API responses.
     * Parses error body for more specific messages if available.
     */
    private Mono<Throwable> processApiError(ClientResponse clientResponse, String defaultMessage) {
        return clientResponse.bodyToMono(String.class)
                .defaultIfEmpty("No error body provided by Shiprocket")
                .flatMap(errorBody -> {
                    HttpStatus httpStatus = (HttpStatus) clientResponse.statusCode();
                    String specificErrorMessage = errorBody;

                    // Attempt to parse a specific error message from JSON if it's a known format
                    try {
                        JsonNode errorNode = objectMapper.readTree(errorBody);
                        if (errorNode.has("message") && errorNode.get("message").isTextual()) {
                            specificErrorMessage = errorNode.get("message").asText();
                        } else if (errorNode.has("errors") && errorNode.get("errors").isObject()) {
                            // Shiprocket often returns validation errors in an "errors" object
                            specificErrorMessage = errorNode.get("errors").toString(); // Or iterate for more detail
                        }
                    } catch (JsonProcessingException e) {
                        log.debug("Shiprocket: Failed to parse error body as JSON, using raw body. Error: {}", e.getMessage());
                    }

                    log.error("Shiprocket API Error | Status: {} | Body: {} | Default Message: {} | Specific Error: {}",
                            httpStatus, errorBody, defaultMessage, specificErrorMessage);

                    // Invalidate token on 401 Unauthorized
                    if (httpStatus == HttpStatus.UNAUTHORIZED) {
                        log.warn("Shiprocket: Received 401 Unauthorized. Invalidating cached token.");
                        this.authToken = null;
                        this.tokenExpiryTime = null;
                    }

                    PartnerApiException exception = new PartnerApiException(
                            defaultMessage + ": " + specificErrorMessage,
                            "SHIPROCKET",
                            httpStatus,
                            specificErrorMessage, // Pass the parsed specific error message
                            null
                    );
                    return Mono.error(exception);
                });
    }

    // --- Public API Methods ---

    /**
     * Creates an order in Shiprocket.
     *
     * @param request The ShiprocketCreateOrderRequest containing order details.
     * @return A Mono emitting ShiprocketCreateOrderResponse.
     */
    public Mono<ShiprocketCreateOrderResponse> createOrder(ShiprocketCreateOrderRequest request) {
        log.info("Shiprocket: Creating order for internal order_id: {}", request.getOrderId());
        try {
            log.debug("Shiprocket: Create Order Request Body: {}", objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            log.warn("Shiprocket: Failed to log create order request body: {}", e.getMessage());
        }

        return getValidAuthToken()
                .flatMap(token -> webClient.post()
                        .uri("/v1/external/orders/create/adhoc")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .bodyValue(request)
                        .retrieve()
                        .onStatus(status -> status.isError() , this.handleCreateOrderError)
                        .bodyToMono(ShiprocketCreateOrderResponse.class))
                .doOnError(e -> log.error("Shiprocket: Exception during order creation for order_id {}: {}", request.getOrderId(), e.getMessage()));
    }

    /**
     * Assigns an AWB to a shipment using Shiprocket API.
     *
     * @param request The ShiprocketAssignAwbRequest containing shipment_id and courier_id.
     * @return Mono emitting ShiprocketAssignAwbResponse.
     */
    public Mono<ShiprocketAssignAwbResponse> assignAwb(ShiprocketAssignAwbRequest request) {
        log.info("Shiprocket: Assigning AWB for shipment_id: {}, courier_id: {}", request, request.getCourier_id());
        try {
            log.debug("Shiprocket: Assign AWB Request Body: {}", objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            log.warn("Shiprocket: Failed to log assign AWB request body: {}", e.getMessage());
        }

        return getValidAuthToken()
                .flatMap(token -> webClient.post()
                        .uri("/v1/external/courier/assign/awb")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .bodyValue(request)
                        .retrieve()
                        .onStatus(status -> status.isError(),
                                clientResponse -> processApiError(clientResponse, "Failed to assign AWB"))
                        .bodyToMono(ShiprocketAssignAwbResponse.class))
                .doOnError(e -> log.error("Shiprocket: Exception during AWB assignment for shipment_id {}: {}", request.getShipment_id(), e.getMessage()));
    }



    /**
     * Cancels an order in Shiprocket by AWB code.
     *
     * @param awbCode The AWB code of the order to cancel.
     * @return A Mono emitting Void upon successful cancellation.
     */
    public Mono<Void> cancelOrder(String awbCode) {
        log.info("Shiprocket: Requesting cancellation for AWB: {}", awbCode);
        ShiprocketCancelRequest cancelRequest = ShiprocketCancelRequest.builder()
                .awbCodes(List.of(awbCode))
                .build();

        try {
            log.debug("Shiprocket: Cancel Order Request Body: {}", objectMapper.writeValueAsString(cancelRequest));
        } catch (JsonProcessingException e) {
            log.warn("Shiprocket: Failed to log cancel order request body: {}", e.getMessage());
        }

        return getValidAuthToken()
                .flatMap(token -> webClient.post()
                        .uri("/v1/external/orders/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .bodyValue(cancelRequest)
                        .retrieve()
                        .onStatus(status -> status.isError() , this.handleCancelOrderError)
                        // Shiprocket cancel response is usually just a success/failure message,
                        // can be mapped to Void if the status indicates success.
                        .bodyToMono(JsonNode.class) // Read as JsonNode to check response, then map to Void
                        .then()) // Consumes the body and completes with Void
                .doOnError(e -> log.error("Shiprocket: Exception during order cancellation for AWB {}: {}", awbCode, e.getMessage()));
    }

    /**
     * Tracks an order in Shiprocket by AWB code.
     *
     * @param awbCode The AWB code of the order to track.
     * @return A Mono emitting the shipment status as a String.
     */
    public Mono<String> trackOrder(String awbCode) {
        log.info("Shiprocket: Tracking AWB: {}", awbCode);
        return getValidAuthToken()
                .flatMap(token -> webClient.get()
                        .uri("/v1/external/courier/track/{awb}", awbCode)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .onStatus(status -> status.isError() , this.handleTrackOrderError)
                        .bodyToMono(ShiprocketTrackingResponse.class)
                        .map(trackingResponse -> {
                            if (trackingResponse == null || trackingResponse.getTrackingData() == null || trackingResponse.getTrackingData().getShipmentStatus() == null) {
                                log.warn("Shiprocket: Tracking response valid but no status for AWB: {}", awbCode);
                                return "UNKNOWN";
                            }
                            log.debug("Shiprocket: Tracking status for AWB {}: {}", awbCode, trackingResponse.getTrackingData().getShipmentStatus());
                            return trackingResponse.getTrackingData().getShipmentStatus();
                        }))
                .doOnError(e -> log.error("Shiprocket: Exception during tracking for AWB {}: {}", awbCode, e.getMessage()));
    }

    /**
     * Checks courier serviceability between pickup and delivery postcodes.
     *
     * @param request The CourierServiceabilityRequest containing details.
     * @return A Mono emitting a List of available DeliveryOption DTOs.
     */
    public Mono<List<DeliveryOption>> checkServiceability(CourierServiceabilityRequest request) {
        log.info("Shiprocket: Checking serviceability for pickup: {} to delivery: {}",
                request.getPickup_postcode(), request.getDelivery_postcode());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Optional.ofNullable(request.getPickup_postcode()).ifPresent(v -> params.add("pickup_postcode", v.toString()));
        Optional.ofNullable(request.getDelivery_postcode()).ifPresent(v -> params.add("delivery_postcode", v.toString()));
        Optional.ofNullable(request.getWeight()).ifPresent(v -> params.add("weight", v.toPlainString()));
        Optional.ofNullable(request.getCod()).ifPresent(v -> params.add("cod", v.toString()));
        Optional.ofNullable(request.getOrder_id()).ifPresent(v -> params.add("order_id", v));
        Optional.ofNullable(request.getLength()).ifPresent(v -> params.add("length", v.toPlainString()));
        Optional.ofNullable(request.getWidth()).ifPresent(v -> params.add("width", v.toPlainString()));
        Optional.ofNullable(request.getHeight()).ifPresent(v -> params.add("height", v.toPlainString()));
        Optional.ofNullable(request.getDeclared_value()).ifPresent(v -> params.add("declared_value", v.toPlainString()));
        Optional.ofNullable(request.getIs_international()).ifPresent(v -> params.add("is_international", v.toString()));
        Optional.ofNullable(request.getItems_count()).ifPresent(v -> params.add("items_count", v.toString()));
        Optional.ofNullable(request.getMode()).ifPresent(v -> params.add("mode", v));
        Optional.ofNullable(request.getCurrency()).ifPresent(v -> params.add("currency", v));
        Optional.ofNullable(request.getSeller_id()).ifPresent(v -> params.add("seller_id", v.toString()));

        String uri = UriComponentsBuilder.fromPath("/v1/external/courier/serviceability")
                .queryParams(params)
                .encode()
                .toUriString();

        log.debug("Shiprocket: Serviceability URI: {}", uri);
        try {
            log.debug("Shiprocket: Serviceability Request Params: {}", objectMapper.writeValueAsString(params));
        } catch (JsonProcessingException e) {
            log.warn("Shiprocket: Failed to log serviceability request params: {}", e.getMessage());
        }

        return getValidAuthToken()
                .flatMap(token -> webClient.get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .onStatus(status -> status.isError() , this.handleServiceabilityError)
                        .bodyToMono(ShiprocketServiceabilityResponse.class)
                        .doOnNext(response -> { // Production-ready debug log
                            try {
                                log.debug("Shiprocket: Full Serviceability Response Body (SUCCESS): {}", objectMapper.writeValueAsString(response));
                            } catch (JsonProcessingException e) {
                                log.warn("Shiprocket: Failed to log serviceability response body: {}", e.getMessage());
                            }
                        })
                        .map(shiprocketResponse -> {
                            // Validate the Shiprocket response, checking the nested 'data' object
                            if (shiprocketResponse == null ||
                                    shiprocketResponse.getData() == null || // Check if 'data' is null
                                    shiprocketResponse.getData().getAvailableCourierCompanies() == null || // Check if list within 'data' is null
                                    shiprocketResponse.getData().getAvailableCourierCompanies().isEmpty()) { // Check if list is empty
                                log.info("Shiprocket: No available courier companies found for request: {}", request);
                                return Collections.<DeliveryOption>emptyList(); // Return empty list on no couriers
                            }
                            // Map Shiprocket's available couriers from the nested 'data' object
                            return shiprocketResponse.getData().getAvailableCourierCompanies().stream()
                                    .map(this::mapShiprocketCourierToDeliveryOption)
                                    .collect(Collectors.toList());
                        }))
                .doOnError(e -> log.error("Shiprocket: Exception checking serviceability for request {}: {}", request, e.getMessage()))
                .defaultIfEmpty(Collections.emptyList());
    }

    /**
     * Helper method to map Shiprocket's specific courier DTO to your internal DeliveryOption DTO.
     */
    private DeliveryOption mapShiprocketCourierToDeliveryOption(ShiprocketAvailableCourierCompany shiprocketCourier) {
        // Use courier_company_id from Shiprocket response as your courierId
        String courierId = Optional.ofNullable(shiprocketCourier.getCourierCompanyId())
                .map(String::valueOf)
                .orElseGet(() -> Optional.ofNullable(shiprocketCourier.getId()) // Fallback to 'id' if 'courier_company_id' is null
                        .map(String::valueOf)
                        .orElse("UNKNOWN"));

        return DeliveryOption.builder()
                .courierId(courierId)
                .carrierName(Optional.ofNullable(shiprocketCourier.getCourierName()).orElse("Unknown Carrier"))
                // Using estimatedDeliveryDays for serviceType/description, parsing the String to Integer
                .serviceType(Optional.ofNullable(shiprocketCourier.getEstimatedDeliveryDays())
                        .filter(s -> !s.isBlank())
                        .map(s -> s + " days delivery")
                        .orElse("Standard Delivery"))
                .description(Optional.ofNullable(shiprocketCourier.getDescription())
                        .filter(s -> !s.isBlank())
                        .orElseGet(() -> Optional.ofNullable(shiprocketCourier.getEstimatedDeliveryDays())
                                .filter(s -> !s.isBlank())
                                .map(s -> "Estimated " + s + " days delivery")
                                .orElse("Standard Delivery Service")))
                .cost(Optional.ofNullable(shiprocketCourier.getRate()).orElse(BigDecimal.ZERO))
                .currency("INR") // Assuming INR, make configurable if needed
                .estimatedDeliveryDays(parseEtdDays(shiprocketCourier.getEstimatedDeliveryDays())) // Use the new field
                .etdRaw(Optional.ofNullable(shiprocketCourier.getEtd()).orElse(null)) // Store raw ETD if useful
                .minWeight(Optional.ofNullable(shiprocketCourier.getMinWeight()).orElse(BigDecimal.ZERO))
                .maxWeight(Optional.ofNullable(shiprocketCourier.getMaxWeight()).orElse(BigDecimal.ZERO))
                .build();
    }

    /**
     * Parses the estimated delivery days string from Shiprocket into an Integer.
     * Handles various formats and provides robust error handling.
     */
    private Integer parseEtdDays(String etdDays) {
        if (etdDays == null || etdDays.isBlank()) {
            return null;
        }
        try {
            // Shiprocket can return "0 days", "1 day", "2-3 days", or just a number like "0"
            // Extract numbers, prioritizing the first number found for a single integer estimate
            String cleanedEtd = etdDays.replaceAll("[^\\d-]", ""); // Keep digits and hyphens
            if (cleanedEtd.contains("-")) {
                // If it's a range like "2-3", take the first number as the minimum estimate
                return Integer.parseInt(cleanedEtd.split("-")[0]);
            } else if (!cleanedEtd.isBlank()) {
                return Integer.parseInt(cleanedEtd);
            }
        } catch (NumberFormatException e) {
            log.warn("Shiprocket: Could not parse etd_days '{}' to integer. Defaulting to null. Error: {}", etdDays, e.getMessage());
        }
        return null;
    }
}