package com.lemicare.delivery.service.controller;

import com.lemicare.delivery.service.client.ShiprocketApiClient;
import com.lemicare.delivery.service.client.dto.CourierServiceabilityRequest;
import com.lemicare.delivery.service.client.dto.DeliveryOption;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/internal") // Internal path prefix
@RequiredArgsConstructor

public class InternalController {
    private final ShiprocketApiClient shiprocketApiClient;
    @PostMapping("/serviceability")
    public Mono<List<DeliveryOption>>  getAvailableCourierService(@RequestBody CourierServiceabilityRequest request ) {
        return shiprocketApiClient.checkServiceability(request);
    }

}
