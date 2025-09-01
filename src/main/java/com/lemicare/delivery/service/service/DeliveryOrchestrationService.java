package com.lemicare.delivery.service.service;

import com.cosmicdoc.common.model.DeliveryOrder;
import com.cosmicdoc.common.model.DeliveryStatus;
import com.cosmicdoc.common.repository.DeliveryOrderRepository;
import com.github.f4b6a3.ulid.UlidCreator;
import com.lemicare.delivery.service.context.TenantContext;
import com.lemicare.delivery.service.dto.request.CreateDeliveryRequest;
import com.lemicare.delivery.service.dto.response.DeliveryResponse;
import com.lemicare.delivery.service.exception.ResourceNotFoundException;
import com.lemicare.delivery.service.strategy.DeliveryStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryOrchestrationService {

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final DeliveryStrategyFactory strategyFactory;

    public DeliveryResponse createDeliveryRequest(CreateDeliveryRequest request) {
        String organizationId = TenantContext.getOrganizationId();
        String branchId = TenantContext.getBranchId();

        deliveryOrderRepository.findByOrderIdAndOrganizationIdAndBranchId(request.getOrderId(), organizationId, branchId)
                .ifPresent(existingOrder -> {
                    log.warn("Attempted to create a duplicate delivery for orderId: {}", request.getOrderId());
                    throw new IllegalStateException("Delivery for orderId " + request.getOrderId() + " already exists.");
                });

        DeliveryOrder newDeliveryOrder = DeliveryOrder.builder()
                .id(UlidCreator.getUlid().toString())
                .orderId(request.getOrderId())
                .organizationId(organizationId)
                .branchId(branchId)
                .partnerName(request.getPreferredPartner())
                .pickupAddress(request.getPickupAddress())
                .dropoffAddress(request.getDropoffAddress())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .status(DeliveryStatus.PENDING)
                .updatedAt(new Date())
                .build();

        // TODO: Add the real logic to call the strategy factory and partner API
        var strategy = strategyFactory.getStrategy(newDeliveryOrder.getPartnerName());
        String trackingId = strategy.createShipment(newDeliveryOrder);
        newDeliveryOrder.setPartnerTrackingId(trackingId);
        newDeliveryOrder.setStatus(DeliveryStatus.ACCEPTED);

        DeliveryOrder savedOrder = deliveryOrderRepository.save(newDeliveryOrder);
        return mapToResponse(savedOrder);
    }

    public DeliveryResponse getDeliveryById(String deliveryId) {
        String callerOrganizationId = TenantContext.getOrganizationId();

        // Fetch the delivery by its primary ID.
        DeliveryOrder deliveryOrder = deliveryOrderRepository.findById( TenantContext.getOrganizationId(), TenantContext.getBranchId(),deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        // CRITICAL SECURITY CHECK: Verify that the fetched resource belongs to the caller's organization.
        if (!deliveryOrder.getOrganizationId().equals(callerOrganizationId)) {
            log.warn("SECURITY ALERT: User from organization '{}' attempted to access delivery '{}' belonging to organization '{}'.",
                    callerOrganizationId, deliveryId, deliveryOrder.getOrganizationId());
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        return mapToResponse(deliveryOrder);
    }

    public List<DeliveryResponse> findDeliveries(DeliveryStatus status) {
        String organizationId = TenantContext.getOrganizationId();
        String branchId = TenantContext.getBranchId();

        List<DeliveryOrder> deliveries;
        if (status != null) {
            deliveries = deliveryOrderRepository.findByOrganizationIdAndBranchIdAndStatus(organizationId, branchId, status);
        } else {
            deliveries = deliveryOrderRepository.findByOrganizationIdAndBranchId(organizationId, branchId);
        }

        return deliveries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DeliveryResponse cancelDelivery(String deliveryId, String reason) {
        String callerOrganizationId = TenantContext.getOrganizationId();

        // Fetch the resource first.
        DeliveryOrder deliveryOrder = deliveryOrderRepository.findById(TenantContext.getOrganizationId(),TenantContext.getBranchId(),deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        // CRITICAL SECURITY CHECK
        if (!deliveryOrder.getOrganizationId().equals(callerOrganizationId)) {
            log.warn("SECURITY ALERT: User from organization '{}' attempted to cancel delivery '{}' belonging to organization '{}'.",
                    callerOrganizationId, deliveryId, deliveryOrder.getOrganizationId());
            throw new AccessDeniedException("You do not have permission to cancel this delivery.");
        }

        // TODO: Add real logic to call the partner strategy to cancel the shipment
        var strategy = strategyFactory.getStrategy(deliveryOrder.getPartnerName());
        strategy.cancelShipment(deliveryOrder, reason);

        deliveryOrder.setStatus(DeliveryStatus.CANCELLED);
        deliveryOrder.setNotes("Cancelled by user. Reason: " + reason);
        deliveryOrder.setUpdatedAt(new Date());

        DeliveryOrder updatedOrder = deliveryOrderRepository.save(deliveryOrder);
        return mapToResponse(updatedOrder);
    }

    private DeliveryResponse mapToResponse(DeliveryOrder order) {
        return DeliveryResponse.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .organizationId(order.getOrganizationId())
                .branchId(order.getBranchId())
                .partnerName(order.getPartnerName())
                .partnerTrackingId(order.getPartnerTrackingId())
                .status(order.getStatus())
                .dropoffAddress(order.getDropoffAddress())
                .recipientName(order.getRecipientName())
                .deliveryFee(order.getDeliveryFee())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }
}
