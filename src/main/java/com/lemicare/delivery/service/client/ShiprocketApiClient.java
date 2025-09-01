package com.lemicare.delivery.service.client;

import com.lemicare.delivery.service.dto.request.ShiprocketAuthRequest;
import com.lemicare.delivery.service.dto.request.ShiprocketCancelRequest;
import com.lemicare.delivery.service.dto.request.ShiprocketCreateOrderRequest;
import com.lemicare.delivery.service.dto.response.ShiprocketAuthResponse;
import com.lemicare.delivery.service.dto.response.ShiprocketCreateOrderResponse;
import com.lemicare.delivery.service.dto.response.ShiprocketTrackingResponse;
import com.lemicare.delivery.service.exception.PartnerApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.ClientResponse; // Explicit import for clarity


import java.util.List;

@Component
@Slf4j
public class ShiprocketApiClient {

    private final WebClient webClient;
    private final String email;
    private final String password;

    // A simple in-memory token cache. For multi-instance deployments, use a distributed cache like Redis.
    private volatile String authToken;

    public ShiprocketApiClient(WebClient shiprocketWebClient,
                               @Value("${shiprocket.api.email}") String email,
                               @Value("${shiprocket.api.password}") String password) {
        this.webClient = shiprocketWebClient;
        this.email = email;
        this.password = password;
    }

    private void authenticate() {
        log.info("Authenticating with Shiprocket API...");
        try {
            ShiprocketAuthRequest authRequest = new ShiprocketAuthRequest(email, password);
            ShiprocketAuthResponse authResponse = webClient.post()
                    .uri("/auth/login")
                    .bodyValue(authRequest)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> // <-- FIX: Renamed to clientResponse
                            handleApiError(clientResponse, "Shiprocket authentication failed")
                    )
                    .bodyToMono(ShiprocketAuthResponse.class)
                    .block();

            if (authResponse == null || authResponse.getToken() == null || authResponse.getToken().isBlank()) {
                throw new PartnerApiException("Received null or empty token from Shiprocket", "SHIPROCKET", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            this.authToken = authResponse.getToken();
            log.info("Successfully authenticated with Shiprocket.");
        } catch (Exception e) {
            log.error("Exception during Shiprocket authentication", e);
            if (e instanceof PartnerApiException) throw (PartnerApiException) e;
            throw new PartnerApiException("Unexpected error during Shiprocket authentication", "SHIPROCKET", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private synchronized String getAuthToken() {
        if (this.authToken == null) {
            authenticate();
        }
        return this.authToken;
    }

    public ShiprocketCreateOrderResponse createOrder(ShiprocketCreateOrderRequest request) {
        String token = getAuthToken();
        log.info("Creating Shiprocket order for internal order_id: {}", request.getOrderId());
        try {
            return webClient.post()
                    .uri("/orders/create/adhoc")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> { // <-- FIX: Renamed to clientResponse
                        if (clientResponse.statusCode().value() == 401) {
                            this.authToken = null;
                            log.warn("Shiprocket token is expired or invalid. Clearing token for re-authentication.");
                        }
                        return handleApiError(clientResponse, "Failed to create Shiprocket order");
                    })
                    .bodyToMono(ShiprocketCreateOrderResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Exception during Shiprocket order creation for order_id: {}", request.getOrderId(), e);
            if (e instanceof PartnerApiException) throw (PartnerApiException) e;
            throw new PartnerApiException("Unexpected error during Shiprocket order creation", "SHIPROCKET", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public void cancelOrder(String awbCode) throws PartnerApiException {
        String token = getAuthToken();
        log.info("Requesting cancellation for Shiprocket AWB: {}", awbCode);
        try {
            ShiprocketCancelRequest cancelRequest = new ShiprocketCancelRequest(List.of(awbCode));
            webClient.post()
                    .uri("/orders/cancel")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .bodyValue(cancelRequest)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> { // <-- FIX: Renamed to clientResponse
                        if (clientResponse.statusCode().value() == 401) this.authToken = null;
                        return handleApiError(clientResponse, "Failed to cancel Shiprocket order");
                    })
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Exception during Shiprocket order cancellation for AWB: {}", awbCode, e);
            if (e instanceof PartnerApiException) throw (PartnerApiException) e;
            throw new PartnerApiException("Unexpected error during Shiprocket order cancellation", "SHIPROCKET", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public String trackOrder(String awbCode) throws PartnerApiException {
        String token = getAuthToken();
        log.info("Tracking Shiprocket AWB: {}", awbCode);
        try {
            ShiprocketTrackingResponse trackingResponse = webClient.get() // <-- FIX: Renamed variable
                    .uri("/courier/track/awb/{awb}", awbCode)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> { // <-- FIX: Renamed to clientResponse
                        if (clientResponse.statusCode().value() == 401) this.authToken = null;
                        return handleApiError(clientResponse, "Failed to track Shiprocket order");
                    })
                    .bodyToMono(ShiprocketTrackingResponse.class)
                    .block();

            if (trackingResponse == null || trackingResponse.getTrackingData() == null || trackingResponse.getTrackingData().getShipmentStatus() == null) {
                log.warn("Shiprocket tracking response was valid but contained no status for AWB: {}", awbCode);
                return "UNKNOWN";
            }
            return trackingResponse.getTrackingData().getShipmentStatus();
        } catch (Exception e) {
            log.error("Exception during Shiprocket tracking for AWB: {}", awbCode, e);
            if (e instanceof PartnerApiException) throw (PartnerApiException) e;
            throw new PartnerApiException("Unexpected error during Shiprocket tracking", "SHIPROCKET", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private Mono<Throwable> handleApiError(ClientResponse clientResponse, String message) { // <-- FIX: Parameter type and name
        return clientResponse.bodyToMono(String.class)
                .defaultIfEmpty("No error body")
                .flatMap(errorBody -> {
                    log.error("Shiprocket API Error | Status: {} | Body: {}", clientResponse.statusCode(), errorBody);
                    PartnerApiException exception = new PartnerApiException(
                            message,
                            "SHIPROCKET",
                            HttpStatus.valueOf(clientResponse.statusCode().value()),
                            errorBody,
                            null
                    );
                    return Mono.error(exception);
                });
    }
}