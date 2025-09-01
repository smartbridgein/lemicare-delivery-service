package com.lemicare.delivery.service.strategy;

import com.cosmicdoc.common.model.DeliveryOrder;
import com.cosmicdoc.common.model.DeliveryStatus;
import com.lemicare.delivery.service.exception.PartnerApiException;

/**
 * The Strategy interface defines the common contract for all delivery partner integrations.
 * Each partner (Shiprocket, Dunzo, etc.) will have its own implementation of this interface.
 * This allows the core business logic to remain completely decoupled from the specifics
 * of any single partner's API.
 */
public interface DeliveryPartnerStrategy {

    /**
     * Returns the unique, consistent name of the partner.
     * This is used by the factory to identify and select this strategy.
     * e.g., "SHIPROCKET", "DUNZO".
     *
     * @return The partner's name.
     */
    String getPartnerName();

    /**
     * Creates a new shipment/delivery request with the external partner.
     * This method is responsible for calling the partner's API, creating the order,
     * and returning the partner's unique tracking ID.
     *
     * @param deliveryOrder The internal DeliveryOrder object containing all necessary details.
     * @return The unique tracking ID provided by the partner.
     * @throws PartnerApiException if the API call fails for any reason.
     */
    String createShipment(DeliveryOrder deliveryOrder) throws PartnerApiException;

    /**
     * Cancels an existing shipment with the external partner.
     *
     * @param deliveryOrder The order to be cancelled (contains the partnerTrackingId).
     * @param reason A brief reason for the cancellation.
     * @throws PartnerApiException if the cancellation fails.
     */
    void cancelShipment(DeliveryOrder deliveryOrder, String reason) throws PartnerApiException;

    /**
     * Fetches the latest status of a shipment from the partner and translates it
     * into our internal, standardized DeliveryStatus enum.
     *
     * @param deliveryOrder The order to check.
     * @return The standardized internal DeliveryStatus.
     * @throws PartnerApiException if the status check fails.
     */
    DeliveryStatus getShipmentStatus(DeliveryOrder deliveryOrder) throws PartnerApiException;
}
