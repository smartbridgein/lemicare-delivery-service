package com.lemicare.delivery.service.client;

import com.lemicare.delivery.service.client.dto.OrderDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * A declarative Feign client for communicating with the order-service.
 */
@FeignClient(name = "order-service", url = "${services.order.url}")
public interface OrderServiceClient {

    /**
     * Fetches enriched order details, including line items and package dimensions.
     */
    @GetMapping("/api/internal/{orgId}/orders/{orderId}")
    OrderDetailsDto getOrderDetails(@PathVariable("orgId") String orgId,@PathVariable("orderId") String orderId);
}
