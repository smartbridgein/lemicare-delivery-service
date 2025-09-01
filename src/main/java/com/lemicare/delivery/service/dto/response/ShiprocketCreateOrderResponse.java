package com.lemicare.delivery.service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShiprocketCreateOrderResponse {
    @JsonProperty("order_id")
    private long shiprocketOrderId;

    @JsonProperty("shipment_id")
    private long shipmentId;

    @JsonProperty("status")
    private String status;

    // This is often what you want to use as the partner tracking ID
    @JsonProperty("awb_code")
    private String awbCode;
}

