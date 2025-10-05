package com.lemicare.delivery.service.controller;

import com.cosmicdoc.common.model.DeliveryStatus;
import com.lemicare.delivery.service.context.TenantContext;
import com.lemicare.delivery.service.dto.request.CancelRequest;
import com.lemicare.delivery.service.dto.request.CreateDeliveryRequest;
import com.lemicare.delivery.service.dto.response.DeliveryResponse;
import com.lemicare.delivery.service.security.SecurityUtils;
import com.lemicare.delivery.service.service.DeliveryOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

/**
 * REST Controller for managing delivery orders, refactored to use TenantContext.
 * Tenancy information (organizationId, branchId) is implicitly retrieved from the
 * TenantContext, which is populated by the TenantFilter from the JWT.
 */
@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final DeliveryOrchestrationService deliveryService;

    /**
     * Creates a new delivery request. The organization and branch are inferred from the JWT.
     *
     * @param request The DTO containing delivery details (without tenant IDs).
     * @return A DTO of the created delivery resource.
     */
    @PostMapping
   // @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<DeliveryResponse> createDeliveryRequest(@Valid @RequestBody CreateDeliveryRequest request) {

        String organizationId = SecurityUtils.getOrganizationId();
        String branchId = SecurityUtils.getBranchId();
        DeliveryResponse response = deliveryService.createDeliveryRequest(organizationId,branchId,request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a single delivery by its unique ID.
     * The service layer is responsible for ensuring the caller belongs to the correct tenant.
     *
     * @param deliveryId The unique ID of the delivery.
     * @return The details of the delivery.
     */
    @GetMapping("/{deliveryId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN','SCOPE_deliveries:read')")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable String deliveryId) {
        log.info("API: Get delivery by ID: {} for org: {}, branch: {}",
                deliveryId, TenantContext.getOrganizationId(), TenantContext.getBranchId());

        DeliveryResponse response = deliveryService.getDeliveryById(deliveryId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a list of deliveries for the caller's branch, inferred from the JWT.
     *
     * @param status The delivery status to filter by (optional).
     * @return A list of matching delivery DTOs.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN','SCOPE_deliveries:read')")
    public ResponseEntity<List<DeliveryResponse>> findDeliveries(
            @RequestParam(required = false) DeliveryStatus status) {

        log.info("API: Searching deliveries with status '{}' for org: {}, branch: {}",
                status, TenantContext.getOrganizationId(), TenantContext.getBranchId());

        List<DeliveryResponse> responses = deliveryService.findDeliveries(status);
        return ResponseEntity.ok(responses);
    }

    /**
     * Cancels an active delivery request.
     * The service layer ensures the caller has permission to cancel this specific delivery.
     *
     * @param deliveryId The ID of the delivery to cancel.
     * @param request A DTO containing the reason for cancellation.
     * @return The updated delivery details.
     */
    @PostMapping("/{deliveryId}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN','SCOPE_deliveries:write')")
    public ResponseEntity<DeliveryResponse> cancelDelivery(
            @PathVariable String deliveryId,
            @Valid @RequestBody CancelRequest request) throws AccessDeniedException {

        log.info("API: Cancel delivery ID: {} for org: {}. Reason: {}",
                deliveryId, TenantContext.getOrganizationId(), request.getReason());

        DeliveryResponse response = deliveryService.cancelDelivery(deliveryId, request.getReason());
        return ResponseEntity.ok(response);
    }
}

